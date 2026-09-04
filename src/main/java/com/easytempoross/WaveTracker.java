package com.easytempoross;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.util.Text;

@Singleton
public class WaveTracker
{
	@Getter
	private boolean incoming;
	@Getter
	private boolean tethered;
	@Getter
	private boolean victory;

	@Inject
	WaveTracker()
	{
	}

	public void reset()
	{
		incoming = false;
		tethered = false;
		victory = false;
	}

	public void setTethered(boolean tethered)
	{
		this.tethered = tethered;
	}

	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE || event.getMessage() == null)
		{
			return;
		}
		String message = Text.removeTags(event.getMessage());
		if (message.contains("A colossal wave closes in"))
		{
			incoming = true;
			return;
		}
		if (message.contains("the rope keeps you securely") || message.contains("the wave slams into you"))
		{
			incoming = false;
			return;
		}
		if (message.contains("The skies clear as Tempoross retreats")
			|| message.contains("ferry you back"))
		{
			victory = true;
		}
	}
}
