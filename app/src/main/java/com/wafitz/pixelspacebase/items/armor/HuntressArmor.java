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
package com.wafitz.pixelspacebase.items.armor;

import java.util.HashMap;

import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.actors.hero.Hero;
import com.wafitz.pixelspacebase.actors.mobs.Mob;
import com.wafitz.pixelspacebase.items.Item;
import com.wafitz.pixelspacebase.items.weapon.missiles.Shuriken;
import com.wafitz.pixelspacebase.levels.Level;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.wafitz.pixelspacebase.utils.GLog;
import com.wafitz.pixelspacebase.actors.hero.HeroClass;
import com.wafitz.pixelspacebase.sprites.MissileSprite;
import com.watabou.utils.Callback;

public class HuntressArmor extends ClassArmor {
	
	private static final String TXT_NO_ENEMIES 		= "No enemies in sight";
	private static final String TXT_NOT_HUNTRESS	= "Only huntresses can use this armor!";
	
	private static final String AC_SPECIAL = "SPECTRAL BLADES"; 
	
	{
		name = "huntress cloak";
		image = ItemSpriteSheet.ARMOR_HUNTRESS;
	}
	
	private HashMap<Callback, Mob> targets = new HashMap<Callback, Mob>();
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		
		Item proto = new Shuriken();
		
		for (Mob mob : Dungeon.level.mobs) {
			if (Level.fieldOfView[mob.pos]) {
				
				Callback callback = new Callback() {	
					@Override
					public void call() {
						Item.curUser.attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							Item.curUser.spendAndNext( Item.curUser.attackDelay() );
						}
					}
				};
				
				((MissileSprite) Item.curUser.sprite.parent.recycle( MissileSprite.class )).
					reset( Item.curUser.pos, mob.pos, proto, callback );
				
				targets.put( callback, mob );
			}
		}
		
		if (targets.size() == 0) {
			GLog.w( TXT_NO_ENEMIES );
			return;
		}
		
		Item.curUser.HP -= (Item.curUser.HP / 3);
		
		Item.curUser.sprite.zap( Item.curUser.pos );
		Item.curUser.busy();
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.HUNTRESS) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_HUNTRESS );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return
			"A huntress in such cloak can create a fan of spectral blades. Each of these blades " +
			"will target a single enemy in the huntress's field of view, inflicting damage depending " +
			"on her currently equipped melee weapon.";
	}
}