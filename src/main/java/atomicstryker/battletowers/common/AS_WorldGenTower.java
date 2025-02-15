package atomicstryker.battletowers.common;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Arrays;
import java.util.Random;

public class AS_WorldGenTower {

    public String failState;

    private static final int[][] candidates = { { 4, -5 }, { 4, 0 }, { 4, 5, }, { 0, -5 }, { 0, 0 }, { 0, 5, }, { -4, -5 },
        { -4, 0, }, { -4, 5 } };

    private static final int candidatecount = candidates.length;
    private static final int maxHoleDepthInBase = 22;
    /**
     *
     */
    public int getChosenTowerOrdinal2(World world, Random random, int ix, int jy, int kz) {
        TowerTypes towerChosen;
        BiomeGenBase biome = world.getBiomeGenForCoordsBody(ix, kz);

        if (biome == BiomeGenBase.icePlains || biome == BiomeGenBase.iceMountains || biome == BiomeGenBase.frozenOcean) {
            towerChosen = TowerTypes.Ice;
        } else if (biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills) {
            towerChosen = TowerTypes.SandStone;
        } else if (biome == BiomeGenBase.swampland) {
            towerChosen = TowerTypes.CobbleStoneMossy;
        } else {
            if (random.nextInt(5) == 0) {
                towerChosen = TowerTypes.SmoothStone;
            } else {
                towerChosen = TowerTypes.CobbleStone;
            }
        }
        return towerChosen.ordinal();
    }
    /**
     * @param world
     * @param random
     * @param ix
     * @param jy
     * @param kz
     * @return -1 when no tower should be able to spawn, else Towerchosen enum ordinal
     * THIS METHOD MAKE SOME CASCADING WORLDGENS BY THE USAGE OF getblock , that's why i made  getChosenTowerOrdinal2 that use directly biomes
     */
    public int getChosenTowerOrdinal(World world, Random random, int ix, int jy, int kz) {
        TowerTypes towerChosen;
        int countWater = 0;
        int countSand = 0;
        int countSnow = 0;
        int countFoliage = 0;
        int countElse = 0;
        int radius = 5;
        for (int ccounter = 0; ccounter < candidatecount; ccounter++) {
            if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
                int[] pair = candidates[ccounter];
                int checkBlockY = getSurfaceBlockHeight(world, ix + pair[0], kz + pair[1]);

                for (int x = ix + pair[0] - radius; x <= ix + pair[0] + radius; x++) {
                    for (int z = kz + pair[1] - radius; z <= kz + pair[1] + radius; z++) {
                        Block currentBlock = world.getBlock(x, checkBlockY, z);

                        if (world.getBlock(x, checkBlockY + 1, z) == Blocks.snow || currentBlock == Blocks.ice) {
                            countSnow++;
                        } else if (currentBlock == Blocks.sand || currentBlock == Blocks.sandstone) {
                            countSand++;
                        } else if (currentBlock == Blocks.water) {
                            countWater++;
                        } else if (currentBlock == Blocks.leaves || currentBlock == Blocks.waterlily || currentBlock == Blocks.log || currentBlock == Blocks.log2) {
                            countFoliage++;
                        } else {
                            countElse++;
                        }
                        if (Math.abs(checkBlockY - jy) > maxHoleDepthInBase) {
                            failState = "Uneven Surface, diff value: " + Math.abs(checkBlockY - jy);
                            return -1;
                        }
                        for (int ycounter2 = 1; ycounter2 <= 3; ycounter2++) {
                            Block aboveBlock = world.getBlock(ix + pair[0], (checkBlockY + ycounter2), kz + pair[1]);
                            if (isBannedBlockID(aboveBlock)) {
                                failState = "Surface banned Block of ID: " + aboveBlock + " at height: " + ycounter2;
                                return -1;
                            }
                        }
                        for (int ycounter = 1; ycounter <= 5; ycounter++) {
                            Block belowBlock = world.getBlock(ix + pair[0], checkBlockY - ycounter, kz + pair[1]);

                            if (belowBlock == Blocks.air || isBannedBlockID(belowBlock)) {
                                failState = "Depth check - Banned Block or hole, Depth: " + ycounter + " ID: " + belowBlock;
                                return -1;
                            }
                        }
                    }
                }
            }
        }
        int[] nums = { countWater, countSnow, countSand, countFoliage, countElse };
        Arrays.sort(nums);
        int result = nums[nums.length - 1];
        if (countSand == result) {
            towerChosen = TowerTypes.SandStone;
        } else if (countSnow == result) {
            towerChosen = TowerTypes.Ice;
        } else if (countWater == result || countFoliage == result) {
            towerChosen = TowerTypes.CobbleStoneMossy;
        } else {
            if (random.nextInt(10) == 0) {
                towerChosen = TowerTypes.Netherrack;
            } else {
                towerChosen = (random.nextInt(5) == 0) ? TowerTypes.SmoothStone : TowerTypes.CobbleStone;
            }
        }
        return towerChosen.ordinal();
    }

    public void generate(World world, int ix, int jy, int kz, int towerchoice, boolean underground) {
        if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
        TowerTypes towerChosen = TowerTypes.values()[towerchoice];

        Block towerWallBlockID = towerChosen.getWallBlockID();
        Block towerLightBlockID = towerChosen.getLightBlockID();
        Block towerFloorBlockID = towerChosen.getFloorBlockID();
        int towerFloorMeta = towerChosen.getFloorBlockMetaData();

        int startingHeight = underground ? Math.max(jy - 70, 15) : jy - 6;
        int maximumHeight = underground ? jy + 7 : 120;

        int floor = 1;
        boolean topFloor = false;
        int builderHeight = startingHeight;
        for (; builderHeight < maximumHeight; builderHeight += 7) // builderHeight jumps floors
        {
            if (builderHeight + 7 >= maximumHeight) {
                topFloor = true;
            }

            for (int floorIterator = 0; floorIterator < 7; floorIterator++) // build each floor height block till next
                                                                            // floor
            {
                if (floor == 1 && floorIterator < 4) // initial floor
                {
                    floorIterator = 4;
                }
                for (int xIterator = -7; xIterator < 7; xIterator++) // do each X
                {
                    for (int zIterator = -7; zIterator < 7; zIterator++) // do each Z
                    {
                        int iCurrent = xIterator + ix;
                        int jCurrent = floorIterator + builderHeight;
                        int zCurrent = zIterator + kz;

                        if (zIterator == -7) // last row, 14
                        {
                            if (xIterator > -5 && xIterator < 4) // rear outer wall
                            {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                            }
                            continue;
                        }
                        if (zIterator == -6 || zIterator == -5) // rows 12 and 13
                        {
                            if (xIterator == -5 || xIterator == 4) // outer wall parts
                            {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                                continue;
                            }
                            if (zIterator == -6) // row 13 extra
                            {
                                if (xIterator == (floorIterator + 1) % 7 - 3) // stairwell!!
                                {
                                    if (!(underground && floor == 1)) {
                                        world.setBlock(
                                            iCurrent,
                                            jCurrent,
                                            zCurrent,
                                            towerChosen.getStairBlockID(),
                                            0,
                                            3);
                                    }
                                    if (floorIterator == 5) {
                                        world.setBlock(iCurrent - 7, jCurrent, zCurrent, towerFloorBlockID, 0, 3);
                                    }
                                    if (floorIterator == 6 && topFloor) // top ledge part
                                    {
                                        buildWallPiece(
                                            world,
                                            iCurrent,
                                            jCurrent,
                                            zCurrent,
                                            towerWallBlockID,
                                            floor,
                                            floorIterator);
                                    }
                                    continue;
                                }
                                if (xIterator < 4 && xIterator > -5) // tower insides
                                {
                                    world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3);
                                }
                                continue;
                            }
                            if (zIterator != -5 || xIterator <= -5 || xIterator >= 5) // outside tower
                            {
                                continue;
                            }
                            if (floorIterator != 0 && floorIterator != 6 || xIterator != -4 && xIterator != 3) {
                                if (floorIterator == 5 && (xIterator == 3 || xIterator == -4)) {
                                    buildFloorPiece(
                                        world,
                                        iCurrent,
                                        jCurrent,
                                        zCurrent,
                                        towerFloorBlockID,
                                        towerFloorMeta);
                                } else {
                                    buildWallPiece(
                                        world,
                                        iCurrent,
                                        jCurrent,
                                        zCurrent,
                                        towerWallBlockID,
                                        floor,
                                        floorIterator); // under stairwell
                                }
                            } else {
                                world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3); // stairwell space
                            }
                            continue;
                        }
                        if (zIterator == -4 || zIterator == -3 || zIterator == 2 || zIterator == 3) // rows 11, 10, 5, 4
                        {
                            if (xIterator == -6 || xIterator == 5) // outer wall parts
                            {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                                continue;
                            }
                            if (xIterator <= -6 || xIterator >= 5) // outside tower
                            {
                                continue;
                            }
                            if (floorIterator == 5) {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                                continue;
                            }
                            if (world.getBlock(iCurrent, jCurrent, zCurrent) != Blocks.chest) // tower inside space
                            {
                                world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3);
                            }
                            continue;
                        }
                        if (zIterator > -3 && zIterator < 2) // rows 10 to 5
                        {
                            if (xIterator == -7 || xIterator == 6) {
                                if (floorIterator < 0 || floorIterator > 3
                                    || ((xIterator != -7 && xIterator != 6) || underground)
                                    || zIterator != -1 && zIterator != 0) // wall, short of window
                                {
                                    buildWallPiece(
                                        world,
                                        iCurrent,
                                        jCurrent,
                                        zCurrent,
                                        towerWallBlockID,
                                        floor,
                                        floorIterator);
                                } else {
                                    world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3);
                                }
                                continue;
                            }
                            if (xIterator <= -7 || xIterator >= 6) {
                                continue;
                            }
                            if (floorIterator == 5) {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            } else {
                                world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3);
                            }
                            continue;
                        }
                        if (zIterator == 4) // row 3
                        {
                            if (xIterator == -5 || xIterator == 4) {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                                continue;
                            }
                            if (xIterator <= -5 || xIterator >= 4) {
                                continue;
                            }
                            if (floorIterator == 5) {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            } else {
                                world.setBlock(iCurrent, jCurrent, zCurrent, Blocks.air, 0, 3);
                            }
                            continue;
                        }
                        if (zIterator == 5) // row 2
                        {
                            if (xIterator == -4 || xIterator == -3 || xIterator == 2 || xIterator == 3) {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                                continue;
                            }
                            if (xIterator <= -3 || xIterator >= 2) {
                                continue;
                            }
                            if (floorIterator == 5) {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            } else {
                                buildWallPiece(
                                    world,
                                    iCurrent,
                                    jCurrent,
                                    zCurrent,
                                    towerWallBlockID,
                                    floor,
                                    floorIterator);
                            }
                            continue;
                        }
                        if (zIterator != 6 || xIterator <= -3 || xIterator >= 2) {
                            continue;
                        }
                        if (floorIterator < 0 || floorIterator > 3 || xIterator != -1 && xIterator != 0) {
                            buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                        } else {
                            buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                        }
                    }

                }
            }

            if (floor == 2) {
                world.setBlock(ix + 3, builderHeight, kz - 5, towerWallBlockID, 0, 3);
                world.setBlock(ix + 3, builderHeight - 1, kz - 5, towerWallBlockID, 0, 3);
            }
            if ((!underground && topFloor) || (underground && floor == 1)) {
                if (towerChosen != TowerTypes.Null) {
                    AS_EntityGolem entitygolem = new AS_EntityGolem(world, towerChosen.ordinal());
                    entitygolem.setLocationAndAngles(
                        ix + 0.5D,
                        builderHeight + 6,
                        kz + 0.5D,
                        world.rand.nextFloat() * 360F,
                        0.0F);
                    world.spawnEntityInWorld(entitygolem);
                }
            } else {
                if (towerChosen != TowerTypes.Null) {
                    world.setBlock(ix + 2, builderHeight + 6, kz + 2, Blocks.mob_spawner, 0, 3);
                    TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) world
                        .getTileEntity(ix + 2, builderHeight + 6, kz + 2);
                    if (tileentitymobspawner != null) {
                        tileentitymobspawner.func_145881_a()
                            .setEntityName(getMobType(world.rand));
                    }

                    world.setBlock(ix - 3, builderHeight + 6, kz + 2, Blocks.mob_spawner, 0, 3);
                    tileentitymobspawner = (TileEntityMobSpawner) world
                        .getTileEntity(ix - 3, builderHeight + 6, kz + 2);
                    if (tileentitymobspawner != null) {
                        tileentitymobspawner.func_145881_a()
                            .setEntityName(getMobType(world.rand));
                    }
                } else {
                    world.setBlock(ix + 2, builderHeight + 6, kz + 2, Blocks.air, 0, 3);
                    world.setBlock(ix - 3, builderHeight + 6, kz + 2, Blocks.air, 0, 3);
                }
            }
            // chest petal
            world.setBlock(ix, builderHeight + 6, kz + 3, towerFloorBlockID, 0, 3);
            world.setBlock(ix - 1, builderHeight + 6, kz + 3, towerFloorBlockID, 0, 3);

            if (builderHeight + 56 >= 120 && floor == 1) {
                floor = 2;
            }

            if (towerChosen != TowerTypes.Null) {
                // chest
                TowerStageItemManager floorChestManager;
                if (!underground) {
                    floorChestManager = topFloor ? WorldGenHandler.getTowerStageManagerForFloor(10, world.rand)
                        : WorldGenHandler.getTowerStageManagerForFloor(floor, world.rand);
                } else {
                    floorChestManager = floor == 1 ? WorldGenHandler.getTowerStageManagerForFloor(10, world.rand)
                        : WorldGenHandler.getTowerStageManagerForFloor(Math.abs(11 - floor), world.rand);
                }

                for (int chestlength = 0; chestlength < 2; chestlength++) {
                    world.setBlock(ix - chestlength, builderHeight + 7, kz + 3, Blocks.chest, 2, 3);
                    TileEntityChest tileentitychest = (TileEntityChest) world
                        .getTileEntity(ix - chestlength, builderHeight + 7, kz + 3);
                    if (tileentitychest != null) {
                        for (int attempt = 0; attempt
                            < (underground ? AS_BattleTowersCore.instance.itemGenerateAttemptsPerFloor * 2
                                : AS_BattleTowersCore.instance.itemGenerateAttemptsPerFloor); attempt++) {
                            ItemStack itemstack = floorChestManager.getStageItem(world.rand, tileentitychest);
                            if (itemstack != null) {
                                tileentitychest.setInventorySlotContents(
                                    world.rand.nextInt(tileentitychest.getSizeInventory()),
                                    itemstack);
                            }
                        }
                    }
                }
            } else {
                for (int chestlength = 0; chestlength < 2; chestlength++) {
                    world.setBlock(ix - chestlength, builderHeight + 7, kz + 3, Blocks.air, 2, 3);
                }
            }

            // move lights builder a bit higher, to support non-opaque lights such as lamps
            world.setBlock(ix + 3, builderHeight + 2, kz - 6, towerLightBlockID, 0, 3);
            world.setBlock(ix - 4, builderHeight + 2, kz - 6, towerLightBlockID, 0, 3);
            world.setBlock(ix + 1, builderHeight + 2, kz - 4, towerLightBlockID, 0, 3);
            world.setBlock(ix - 2, builderHeight + 2, kz - 4, towerLightBlockID, 0, 3);

            if (towerChosen != TowerTypes.Null) {
                for (int l3 = 0; l3 < (floor * 4 + towerChosen.ordinal()) - 8 && !topFloor; l3++) // random hole poker
                {
                    int k4 = 5 - world.rand.nextInt(12);
                    int k5 = builderHeight + 5;
                    int j6 = 5 - world.rand.nextInt(10);
                    if (j6 < -2 && k4 < 4 && k4 > -5 && k4 != 1 && k4 != -2) {
                        continue;
                    }
                    k4 += ix;
                    j6 += kz;
                    if (world.getBlock(k4, k5, j6) == towerFloorBlockID
                        && world.getBlock(k4, k5 + 1, j6) != Blocks.mob_spawner) {
                        world.setBlock(k4, k5, j6, Blocks.air, 0, 3);
                    }
                }
            }

            floor++;
        }

        System.out.println(
            "Battle Tower type " + towerChosen
                + " spawned at [ "
                + ix
                + " | "
                + kz
                + " ], underground: "
                + underground);
        }
    }

    private void buildFloorPiece(World world, int i, int j, int k, Block towerFloorBlockID, int towerFloorMeta) {
        world.setBlock(i, j, k, towerFloorBlockID, 0, 3);

        if (towerFloorMeta != 0) {
            world.setBlockMetadataWithNotify(i, j, k, towerFloorMeta, 3);
        }
    }

    private void buildWallPiece(World world, int i, int j, int k, Block towerWallBlockID, int floor,
        int floorIterator) {
        if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
        world.setBlock(i, j, k, towerWallBlockID, 0, 3);
        if (floor == 1 && floorIterator == 4) {
            fillTowerBaseToGround(world, i, j, k, towerWallBlockID);
        }
        }
    }

    private void fillTowerBaseToGround(World world, int i, int j, int k, Block blocktype) {
        int x = j - 1;
        while (x > 0 && !isBuildableBlockID(world.getBlock(i, x, k))) {
            world.setBlock(i, x, k, blocktype, 0, 3);
            x--;
        }
    }

    private int getSurfaceBlockHeight(World world, int x, int z) {
        int h = 50;
        if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
            while (h < world.getHeightValue(x, z) &&
                !isFoliageBlockID(world.getBlock(x, h, z))) {
                h++;
            }
        }
        return Math.max(0, h - 1);
    }

    private boolean isFoliageBlockID(Block block) {
        return (block == Blocks.snow || block == Blocks.tallgrass
            || block == Blocks.deadbush
            || block == Blocks.log
            || block == Blocks.log2
            || block == Blocks.leaves);
    }

    private boolean isBuildableBlockID(Block ID) {
        return (ID == Blocks.stone || ID == Blocks.grass
            || ID == Blocks.sand
            || ID == Blocks.sandstone
            || ID == Blocks.gravel
            || ID == Blocks.dirt);
    }

    private boolean isBannedBlockID(Block iD) {
        return (iD == Blocks.yellow_flower || iD == Blocks.red_flower
            || iD == Blocks.brown_mushroom_block
            || iD == Blocks.red_mushroom_block
            || iD == Blocks.cactus
            || iD == Blocks.pumpkin
            || iD == Blocks.lava);
    }

    private String getMobType(Random random) {
        switch (random.nextInt(4)) {
            case 0: {
                return "Skeleton";
            }
            case 1: {
                return "Zombie";
            }
            case 2: {
                return "Spider";
            }
            case 3: {
                return "CaveSpider";
            }
            default:
                return "Zombie";
        }
    }

    public enum TowerTypes {

        Null(Blocks.air, Blocks.air, Blocks.air, 0, Blocks.air),
        CobbleStone(Blocks.cobblestone, Blocks.torch, Blocks.double_stone_slab, 0, Blocks.stone_stairs),
        CobbleStoneMossy(Blocks.mossy_cobblestone, Blocks.torch, Blocks.double_stone_slab, 0, Blocks.stone_stairs),
        SandStone(Blocks.sandstone, Blocks.torch, Blocks.double_stone_slab, 1, Blocks.sandstone_stairs),
        Ice(Blocks.ice, Blocks.air /* Blocks.glowStone */, Blocks.clay, 2, Blocks.oak_stairs), // since when does
                                                                                               // glowstone melt ice
        SmoothStone(Blocks.stone, Blocks.torch, Blocks.double_stone_slab, 3, Blocks.stone_stairs),
        Netherrack(Blocks.netherrack, Blocks.glowstone, Blocks.soul_sand, 0, Blocks.nether_brick_stairs),
        Jungle(Blocks.mossy_cobblestone, Blocks.web, Blocks.dirt, 0, Blocks.jungle_stairs);

        private final Block wallBlockID;
        private final Block lightBlockID;
        private final Block floorBlockID;
        private final int floorBlockMetaData;
        private final Block stairBlockID;

        TowerTypes(Block a, Block b, Block c, int d, Block e) {
            this.wallBlockID = a;
            this.lightBlockID = b;
            this.floorBlockID = c;
            this.floorBlockMetaData = d;
            this.stairBlockID = e;
        }

        Block getWallBlockID() {
            return wallBlockID;
        }

        Block getLightBlockID() {
            return lightBlockID;
        }

        Block getFloorBlockID() {
            return floorBlockID;
        }

        int getFloorBlockMetaData() {
            return floorBlockMetaData;
        }

        Block getStairBlockID() {
            return stairBlockID;
        }
    }

}
