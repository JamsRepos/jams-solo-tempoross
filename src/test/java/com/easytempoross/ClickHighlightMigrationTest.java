package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClickHighlightMigrationTest
{
	@Test
	public void oldCheckboxMapsToTheDropdown()
	{
		assertEquals(ClickHighlight.THIS_AND_NEXT, ClickHighlightMigration.fromLegacy(true));
		assertEquals(ClickHighlight.OFF, ClickHighlightMigration.fromLegacy(false));
	}

	@Test
	public void modesKnowWhichOutlinesTheyDraw()
	{
		assertTrue(ClickHighlight.THIS.showsThis());
		assertFalse(ClickHighlight.THIS.showsNext());
		assertTrue(ClickHighlight.THIS_AND_NEXT.showsThis());
		assertTrue(ClickHighlight.THIS_AND_NEXT.showsNext());
		assertFalse(ClickHighlight.NEXT.showsThis());
		assertTrue(ClickHighlight.NEXT.showsNext());
		assertFalse(ClickHighlight.OFF.showsThis());
		assertFalse(ClickHighlight.OFF.showsNext());
	}
}
