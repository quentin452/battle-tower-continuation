package atomicstryker.battletowers.common;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

import org.apache.logging.log4j.Level;

import atomicstryker.battletowers.common.WorldGenHandler.TowerPosition;
import cpw.mods.fml.common.FMLCommonHandler;

public class CommandDeleteBattleTower extends CommandBattleTowers {

    @Override
    public String getCommandName() {
        return "deletebattletower";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/deletebattletower deletes the nearest existing Battletower, given x,z coordinates";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) {
        if (astring.length < 2) {
            throw new WrongUsageException(
                "Invalid Usage of Battletower delete command, must provide x,z coordinates",
                (Object) astring);
        } else {
            try {
                int x = Integer.parseInt(astring[0]);
                int z = Integer.parseInt(astring[1]);
                TowerPosition tp = WorldGenHandler.deleteNearestTower(icommandsender.getEntityWorld(), x, z);
                if (tp != null) {
                    FMLCommonHandler.instance()
                        .getFMLLogger()
                        .log(
                            Level.INFO,
                            icommandsender.getCommandSenderName() + ": Battletower deleted: " + tp);
                    for (Object o : icommandsender.getEntityWorld()
                        .getEntitiesWithinAABB(
                            AS_EntityGolem.class,
                            AxisAlignedBB.getBoundingBox(tp.x - 10, 0.0D, tp.z - 10, tp.x + 10, 255, tp.z + 10))) {
                        ((Entity) o).setDead();
                        break;
                    }
                } else {
                    FMLCommonHandler.instance()
                        .getFMLLogger()
                        .log(
                            Level.INFO,
                            icommandsender.getCommandSenderName() + ": no Battletower deleted, no valid target");
                }
            } catch (Exception e) {
                throw new WrongUsageException(
                    "Invalid Usage of Battletower delete command, must provide x,z coordinates",
                    (Object) astring);
            }
        }
    }

}
