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
package com.wafitz.pixelspacebase.levels;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.DungeonTilemap;
import com.wafitz.pixelspacebase.actors.mobs.npcs.Ghost;
import com.wafitz.pixelspacebase.items.DewVial;
import com.wafitz.pixelspacebase.items.Generator;
import com.wafitz.pixelspacebase.items.Heap;
import com.wafitz.pixelspacebase.items.armor.ClothArmor;
import com.wafitz.pixelspacebase.items.bags.Keyring;
import com.wafitz.pixelspacebase.items.food.Food;
import com.wafitz.pixelspacebase.items.scrolls.ScrollOfMagicMapping;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class SewerLevel extends RegularLevel {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

    public static void addVisuals(Level level, Scene scene) {
        for (int i = 0; i < level.length(); i++) {
            if (level.map[i] == Terrain.WALL_DECO) {
                scene.add(new Sink(i));
            }
        }
    }

    @Override
	public String tilesTex() {
		return Assets.TILES_SEWERS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}

	protected boolean[] water() {
        return Patch.generate(this, feeling == Feeling.WATER ? 0.60f : 0.45f, 5);
    }

    protected boolean[] grass() {
        return Patch.generate(this, feeling == Feeling.GRASS ? 0.60f : 0.40f, 4);
    }

    @Override
    protected void decorate() {

        for (int i = 0; i < width(); i++) {
            if (map[i] == Terrain.WALL &&
                    map[i + width()] == Terrain.WATER &&
                    Random.Int(4) == 0) {

                map[i] = Terrain.WALL_DECO;
            }
        }

        for (int i = width(); i < length() - width(); i++) {
            if (map[i] == Terrain.WALL &&
                    map[i - width()] == Terrain.WALL &&
                    map[i + width()] == Terrain.WATER &&
                    Random.Int(2) == 0) {

                map[i] = Terrain.WALL_DECO;
            }
        }

        for (int i = width() + 1; i < length() - width() - 1; i++) {
            if (map[i] == Terrain.EMPTY) {

                int count =
                        (map[i + 1] == Terrain.WALL ? 1 : 0) +
                                (map[i - 1] == Terrain.WALL ? 1 : 0) +
                                (map[i + width()] == Terrain.WALL ? 1 : 0) +
                                (map[i - width()] == Terrain.WALL ? 1 : 0);

                if (Random.Int(16) < count * count) {
                    map[i] = Terrain.EMPTY_DECO;
                }
            }
        }

        placeSign();

        if (Dungeon.depth <= 1) {
            int belongings = pointToCell(roomEntrance.random());
            if (belongings != Terrain.SIGN) {
                //pos+1 == Terrain.WALL ? pos-1 : pos+1
                drop(Generator.random(), belongings).type = Heap.Type.CHEST;
                drop(new ClothArmor().identify(), belongings);
                drop(new Food().identify(), belongings);
                drop(new Keyring(), belongings);
                // Testing
                drop(new ScrollOfMagicMapping().identify(), belongings);
            }
        }
    }
	
	@Override
	protected void createMobs() {
		super.createMobs();

		Ghost.Quest.spawn( this );
	}
	
	@Override
	protected void createItems() {
		if (Dungeon.dewVial && Random.Int( 4 - Dungeon.depth ) == 0) {
			addItemToSpawn( new DewVial() );
			Dungeon.dewVial = false;
		}

		super.createItems();
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		super.addVisuals( scene );
		addVisuals( this, scene );
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
			return "Murky water";
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
			return "Wet yellowish moss covers the floor.";
		case Terrain.BOOKSHELF:
			return "The bookshelf is packed with cheap useless books. Might it burn?";
		default:
			return super.tileDesc( tile );
		}
	}
	
	private static class Sink extends Emitter {
		
		private static final Emitter.Factory factory = new Factory() {

            @Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				WaterParticle p = (WaterParticle)emitter.recycle( WaterParticle.class );
				p.reset( x, y );
			}
		};
        private int pos;
        private float rippleDelay = 0;

        public Sink( int pos ) {
			super();
			
			this.pos = pos;
			
			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 2, p.y + 1, 4, 0 );
			
			pour( factory, 0.05f );
		}
		
		@Override
		public void update() {
			if (visible = Dungeon.visible[pos]) {
				
				super.update();
				
				if ((rippleDelay -= Game.elapsed) <= 0) {
                    GameScene.ripple(pos + Dungeon.level.width()).y -= DungeonTilemap.SIZE / 2;
                    rippleDelay = Random.Float(0.2f, 0.3f);
                }
            }
        }
    }

	public static final class WaterParticle extends PixelParticle {
		
		public WaterParticle() {
			super();
			
			acc.y = 50;
			am = 0.5f;
			
			color( ColorMath.random( 0xb6ccc2, 0x3b6653 ) );
			size( 2 );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.x = x;
			this.y = y;
			
			speed.set( Random.Float( -2, +2 ), 0 );
			
			left = lifespan = 0.5f;
		}
	}
}
