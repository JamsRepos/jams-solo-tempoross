package com.easytempoross;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Folds the old "highlight next click" checkbox into {@link ClickHighlight}, so turning the
 * highlight off is not reset by the upgrade.
 */
@Slf4j
@Singleton
class ClickHighlightMigration
{
	private final ConfigManager configManager;

	@Inject
	ClickHighlightMigration(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	void run()
	{
		if (configManager.getConfiguration(EasyTemporossConfig.GROUP, EasyTemporossConfig.CLICK_HIGHLIGHT_KEY) != null)
		{
			return;
		}

		String legacy = configManager.getConfiguration(
			EasyTemporossConfig.GROUP, EasyTemporossConfig.LEGACY_HIGHLIGHT_NEXT_CLICK_KEY);
		if (legacy == null)
		{
			return;
		}

		ClickHighlight highlight = fromLegacy(Boolean.parseBoolean(legacy));
		configManager.setConfiguration(
			EasyTemporossConfig.GROUP, EasyTemporossConfig.CLICK_HIGHLIGHT_KEY, highlight);
		configManager.unsetConfiguration(
			EasyTemporossConfig.GROUP, EasyTemporossConfig.LEGACY_HIGHLIGHT_NEXT_CLICK_KEY);
		log.debug("migrated highlight checkbox to {}", highlight);
	}

	static ClickHighlight fromLegacy(boolean enabled)
	{
		return enabled ? ClickHighlight.THIS_AND_NEXT : ClickHighlight.OFF;
	}
}
