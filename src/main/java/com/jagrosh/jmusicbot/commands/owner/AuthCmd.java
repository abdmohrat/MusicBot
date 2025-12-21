package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.http.YoutubeOauth2Handler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthCmd extends OwnerCommand
{
    private static final AtomicBoolean IN_PROGRESS = new AtomicBoolean(false);

    private final Bot bot;

    public AuthCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "auth";
        this.help = "links YouTube OAuth2 (helps with age-restricted content)";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(bot.getConfig().useYoutubeOauth2())
        {
            event.replySuccess("YouTube OAuth2 is already enabled (a refresh token is configured).");
            return;
        }

        YoutubeAudioSourceManager yt = bot.getPlayerManager().getYoutubeSourceManager();
        if(yt == null)
        {
            event.replyError("YouTube source manager is not initialized.");
            return;
        }

        if(!IN_PROGRESS.compareAndSet(false, true))
        {
            event.replyWarning("YouTube OAuth2 linking is already in progress.");
            return;
        }

        event.getAuthor().openPrivateChannel().queue(pc ->
        {
            pc.sendMessage("Starting YouTube OAuth2 linking.\n\n"
                    + "IMPORTANT: Use a burner/secondary Google account, not your main account.\n"
                    + "I will send you a verification URL and code next.").queue();

            Thread t = new Thread(() ->
            {
                try
                {
                    YoutubeOauth2Handler oauth = yt.getOauth2Handler();
                    JsonBrowser device = oauth.fetchDeviceCode();

                    String url = device.get("verification_url").text();
                    String userCode = device.get("user_code").text();
                    String deviceCode = device.get("device_code").text();

                    if(url == null || userCode == null || deviceCode == null || url.isEmpty() || userCode.isEmpty() || deviceCode.isEmpty())
                    {
                        pc.sendMessage("Failed to start OAuth2 linking (missing device flow fields).").queue();
                        return;
                    }

                    pc.sendMessage("Go to: `" + url + "`\nEnter code: `" + userCode + "`").queue();
                    pc.sendMessage("Waiting for you to complete authorization...").queue();

                    long intervalSeconds = device.get("interval").asLong(5);
                    long expiresInSeconds = device.get("expires_in").asLong(600);
                    long intervalMillis = Math.max(1000L, intervalSeconds * 1000L);
                    long deadline = System.currentTimeMillis() + Math.max(30_000L, Math.min(TimeUnit.MINUTES.toMillis(10), expiresInSeconds * 1000L));

                    String refreshToken = null;
                    while(System.currentTimeMillis() < deadline)
                    {
                        try
                        {
                            JsonBrowser tokenResponse = oauth.fetchRefreshToken(deviceCode);
                            refreshToken = tokenResponse.get("refresh_token").text();
                            if(refreshToken != null && !refreshToken.trim().isEmpty())
                                break;
                        }
                        catch(IOException ignored)
                        {
                            // Usually "authorization_pending" until the user completes the flow.
                        }
                        catch(Exception ignored)
                        {
                            // Keep polling until deadline; we'll report timeout if it never succeeds.
                        }

                        try
                        {
                            Thread.sleep(intervalMillis);
                        }
                        catch(InterruptedException ignored)
                        {
                            break;
                        }
                    }

                    if(refreshToken == null || refreshToken.trim().isEmpty())
                    {
                        pc.sendMessage("OAuth2 linking timed out (or was denied). Run `!auth` again to retry.").queue();
                        return;
                    }

                    try
                    {
                        bot.getConfig().persistYoutubeOauth2RefreshToken(refreshToken);
                    }
                    catch(IOException ex)
                    {
                        pc.sendMessage("Linked successfully, but I couldn't write the refresh token to your config file.\n"
                                + "Add this to your `config.txt` manually:\n"
                                + "`youtube.oauth2.enabled = true`\n"
                                + "`youtube.oauth2.refreshToken = \"" + refreshToken.replace("`", "\\`") + "\"`").queue();
                    }

                    yt.useOauth2(refreshToken, true);
                    pc.sendMessage("YouTube OAuth2 linked successfully. Age-restricted content should now work.").queue();
                }
                catch(Exception ex)
                {
                    pc.sendMessage("OAuth2 linking failed: `" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + "`").queue();
                }
                finally
                {
                    IN_PROGRESS.set(false);
                }
            }, "jmusicbot-youtube-oauth");
            t.setDaemon(true);
            t.start();

            event.replySuccess("Check your DMs to finish YouTube OAuth2 linking.");
        }, fail ->
        {
            IN_PROGRESS.set(false);
            event.replyError("I couldn't DM you. Please enable DMs from server members and try again.");
        });
    }
}
