package com.cyborgmas.isingmodel;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Random;

@Mod(IsingModel.MOD_ID)
public class IsingModel {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "ising_model";
    public static final Random RANDOM = new Random();

    public IsingModel() {
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        SimulationCommand.register(event.getDispatcher());
    }
}
