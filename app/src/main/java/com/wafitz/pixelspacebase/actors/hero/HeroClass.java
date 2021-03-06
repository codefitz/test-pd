/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.wafitz.pixelspacebase.actors.hero;

import android.util.Log;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Badges;
import com.wafitz.pixelspacebase.items.TomeOfMastery;
import com.wafitz.pixelspacebase.items.potions.PotionOfStrength;
import com.wafitz.pixelspacebase.items.rings.RingOfShadows;
import com.wafitz.pixelspacebase.items.scrolls.ScrollOfIdentify;
import com.wafitz.pixelspacebase.items.scrolls.ScrollOfMagicMapping;
import com.wafitz.pixelspacebase.items.wands.WandOfMagicMissile;
import com.wafitz.pixelspacebase.items.weapon.melee.Dagger;
import com.wafitz.pixelspacebase.items.weapon.melee.Knuckles;
import com.wafitz.pixelspacebase.items.weapon.melee.ShortSword;
import com.wafitz.pixelspacebase.items.weapon.missiles.Boomerang;
import com.wafitz.pixelspacebase.items.weapon.missiles.Dart;
import com.wafitz.pixelspacebase.ui.QuickSlot;
import com.watabou.utils.Bundle;
// Adding armor kit for testing


public enum HeroClass {

	WARRIOR( "Captain" ), MAGE( "DM3000" ), ROGUE( "Shapeshifter" ), HUNTRESS( "First Officer" );
	
	private String title;
	
	HeroClass(String title) {
		this.title = title;
	}
	
	public static final String[] WAR_PERKS = {
		"Captains start with 11 points of Strength.",
		"Captains start with a unique short sword. This sword can be later \"reforged\" to upgrade another melee weapon.",
		"Captains are less proficient with missile weapons.",
		"Any piece of food restores some health when eaten.",
		"Potions of Strength are identified from the beginning.",
	};
	
	public static final String[] MAG_PERKS = {
		"DM3000 starts with a unique Wand of Magic Missile. This wand can be later \"disenchanted\" to upgrade another wand.",
		"DM3000 units recharge their wands faster.",
		"When eaten, any piece of food restores 1 charge for all wands in the inventory.",
		"DM3000 units can use wands as a melee weapon.",
		"Scrolls of Identify are identified from the beginning."
	};
	
	public static final String[] ROG_PERKS = {
		"Shapeshifters start with a Ring of Shadows+1.",
		"Shapeshifters identify a type of a ring on equipping it.",
		"Shapeshifters are proficient with light armor, dodging better while wearing one.",
		"Shapeshifters are proficient in detecting hidden doors and traps.",
		"Shapeshifters can go without food longer.",
		"Scrolls of Magic Mapping are identified from the beginning."
	};
	
	public static final String[] HUN_PERKS = {
		"Officers start with 15 points of Health.",
		"Officers start with a unique upgradeable boomerang.",
		"Officers are proficient with missile weapons and get a damage bonus for excessive strength when using them.",
		"Officers gain more health from dewdrops.",
		"Officers sense neighbouring monsters even if they are hidden behind obstacles."
	};
	
	public void initHero( Hero hero ) {
		
		hero.heroClass = this;
		
		initCommon( hero );
		
		switch (this) {
		case WARRIOR:
			initWarrior( hero );
			break;
			
		case MAGE:
			initMage( hero );
			break;
			
		case ROGUE:
			initRogue( hero );
			break;
			
		case HUNTRESS:
			initHuntress( hero );
			break;
		}
		
		if (Badges.isUnlocked( masteryBadge() )) {
			new TomeOfMastery().collect();
		}
		
		hero.updateAwareness();

		Log.d("WAFITZ", "Hero is: " + hero.heroClass );
	}
	
	private static void initCommon( Hero hero ) {

		//(hero.belongings.armor = new ClothArmor()).identify();
		/*new Food().identify().collect();
		new Keyring().collect();
		// Testing
		new ArmorKit().identify().collect();
		new LeatherArmor().identify().collect();
		new MailArmor().identify().collect();
		new PlateArmor().identify().collect();
		new ScaleArmor().identify().collect();
		new ScrollOfMagicMapping().identify().collect();*/
	}
	
	public Badges.Badge masteryBadge() {
		switch (this) {
		case WARRIOR:
			return Badges.Badge.MASTERY_WARRIOR;
		case MAGE:
			return Badges.Badge.MASTERY_MAGE;
		case ROGUE:
			return Badges.Badge.MASTERY_ROGUE;
		case HUNTRESS:
			return Badges.Badge.MASTERY_HUNTRESS;
		}
		return null;
	}
	
	private static void initWarrior( Hero hero ) {
		hero.STR = hero.STR + 1;
		
		(hero.belongings.weapon = new ShortSword()).identify();
		new Dart( 8 ).identify().collect();

		QuickSlot.primaryValue = Dart.class;
		
		new PotionOfStrength().setKnown();
	}
	
	private static void initMage( Hero hero ) {	
		(hero.belongings.weapon = new Knuckles()).identify();
		
		WandOfMagicMissile wand = new WandOfMagicMissile();
		wand.identify().collect();
		
		QuickSlot.primaryValue = wand;
		
		new ScrollOfIdentify().setKnown();
	}
	
	private static void initRogue( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();
		(hero.belongings.ring1 = new RingOfShadows()).upgrade().identify();
		new Dart( 8 ).identify().collect();
		
		hero.belongings.ring1.activate( hero );
		
		QuickSlot.primaryValue = Dart.class;
		
		new ScrollOfMagicMapping().setKnown();
	}
	
	private static void initHuntress( Hero hero ) {
		
		hero.HP = (hero.HT -= 5);
		
		(hero.belongings.weapon = new Dagger()).identify();
		Boomerang boomerang = new Boomerang();
		boomerang.identify().collect();
		
		QuickSlot.primaryValue = boomerang;
	}
	
	public String title() {
		return title;
	}
	
	public String spritesheet() {
		
		switch (this) {
		case WARRIOR:
			return Assets.WARRIOR;
		case MAGE:
			return Assets.MAGE;
		case ROGUE:
			return Assets.ROGUE;
		case HUNTRESS:
			return Assets.HUNTRESS;
		}
		
		return null;
	}
	
	public String[] perks() {
		
		switch (this) {
		case WARRIOR:
			return WAR_PERKS;
		case MAGE:
			return MAG_PERKS;
		case ROGUE:
			return ROG_PERKS;
		case HUNTRESS:
			return HUN_PERKS;
		}
		
		return null;
	}

	private static final String CLASS	= "class";
	
	public void storeInBundle( Bundle bundle ) {
		bundle.put( CLASS, toString() );
	}
	
	public static HeroClass restoreInBundle( Bundle bundle ) {
		String value = bundle.getString( CLASS );
		return value.length() > 0 ? valueOf( value ) : ROGUE;
	}
}
