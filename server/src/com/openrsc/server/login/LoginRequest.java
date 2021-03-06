package com.openrsc.server.login;

import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.net.ThrottleFilter;
import com.openrsc.server.util.rsc.DataConversions;
import io.netty.channel.Channel;

/**
 * Container for all the login data which will be used to construct a player
 *
 * @author n0m
 */
public abstract class LoginRequest extends ThrottleFilter {

	protected Player loadedPlayer;
	private String ipAddress;
	private String username;
	private String password;
	private long usernameHash;
	private Channel channel;
	private int clientVersion;


	protected LoginRequest(String username, String password, int clientVersion, Channel channel) {
		this.setUsername(username);
		this.setPassword(password);
		this.setIpAddress(channel.remoteAddress().toString());
		this.setClientVersion(clientVersion);
		this.setUsernameHash(DataConversions.usernameToHash(username));
		this.setChannel(channel);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	private void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	public long getUsernameHash() {
		return usernameHash;
	}

	private void setUsernameHash(long usernameHash) {
		this.usernameHash = usernameHash;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public int getClientVersion() {
		return clientVersion;
	}

	private void setClientVersion(int clientVersion) {
		this.clientVersion = clientVersion;
	}

	public abstract void loginValidated(int response);

	public abstract void loadingComplete(Player loadedPlayer);
}
