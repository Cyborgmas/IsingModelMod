package com.cyborgmas.isingmodel;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SimulationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ising_model")
                        .then(Commands.argument("size", IntegerArgumentType.integer(0))
                                .then(Commands.argument("temperature", DoubleArgumentType.doubleArg(0))
                                        .executes(s -> execute(s.getSource(), IntegerArgumentType.getInteger(s, "size"), DoubleArgumentType.getDouble(s, "temperature")))
                                        .then(Commands.argument("dimension", IntegerArgumentType.integer(2, 3))
                                                .executes(s -> execute(s.getSource(), IntegerArgumentType.getInteger(s, "size"), DoubleArgumentType.getDouble(s, "temperature"), IntegerArgumentType.getInteger(s, "dimension")))
                                                .then(Commands.argument("flips_order_of_magnitude", IntegerArgumentType.integer(0))
                                                        .executes(s -> execute(s.getSource(), IntegerArgumentType.getInteger(s, "size"), DoubleArgumentType.getDouble(s, "temperature"), IntegerArgumentType.getInteger(s, "dimension"), IntegerArgumentType.getInteger(s, "flips_order_of_magnitude")))
                                                        .then(Commands.argument("spin_up", BlockStateArgument.block())
                                                                .then(Commands.argument("spin_down", BlockStateArgument.block())
                                                                        .executes(s -> execute(s.getSource(), IntegerArgumentType.getInteger(s, "size"), DoubleArgumentType.getDouble(s, "temperature"), IntegerArgumentType.getInteger(s, "dimension"), IntegerArgumentType.getInteger(s, "flips_order_of_magnitude"), BlockStateArgument.getBlock(s, "spin_up").getState(), BlockStateArgument.getBlock(s, "spin_down").getState()))
                                                                        .then(Commands.argument("simulate", BoolArgumentType.bool())
                                                                                .executes(s -> execute(s.getSource(), IntegerArgumentType.getInteger(s, "size"), DoubleArgumentType.getDouble(s, "temperature"), IntegerArgumentType.getInteger(s, "dimension"), IntegerArgumentType.getInteger(s, "flips_order_of_magnitude"), BlockStateArgument.getBlock(s, "spin_up").getState(), BlockStateArgument.getBlock(s, "spin_down").getState(), BoolArgumentType.getBool(s, "simulate")))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    public static int execute(CommandSourceStack sourceStack, int size, double temperature) {
        return execute(sourceStack, size, temperature, 2);
    }

    public static int execute(CommandSourceStack sourceStack, int size, double temperature, int dimensions) {
        return execute(sourceStack, size, temperature, dimensions, -1);
    }

    public static int execute(CommandSourceStack sourceStack, int size, double temperature, int dimensions, int flips) {
        return execute(sourceStack, size, temperature, dimensions, flips, Blocks.WHITE_CONCRETE.defaultBlockState(), Blocks.BLACK_CONCRETE.defaultBlockState());
    }

    public static int execute(CommandSourceStack source, int size, double temperature, int dimensions, int flips, BlockState spinUp, BlockState spinDown) {
        return execute(source, size, temperature, dimensions,flips, spinUp, spinDown, true);
    }

    public static int execute(CommandSourceStack source, int size, double temperature, int dimensions, int flips, BlockState spinUp, BlockState spinDown, boolean simulate) {
        if (dimensions == 2)
            return execute2D(source, size, temperature, flips, spinUp, spinDown, simulate);
        else
            return execute3D(source, size, temperature, flips, spinUp, spinDown, simulate);
    }

    /**
     *
     * @param sourceStack   Minecraft context (e.g. where/who executes the command)
     * @param size          Size of the model
     * @param temperature   dimensionless "temperature" := K_B * T / J
     * @param flips         Nb of flips.
     *                      If "-1" uses default simulation value of 100 * size ^ (dimensions)
     *                      Otherwise represents the power of 10 nb of flips,
     *                      a value of 6 means a million flips
     * @param spinUp        The block representing up spins
     * @param spinDown      The block representing down spins
     * @param simulate      Whether to simulate the model (i.e. purely random spins or simulation)
     * @return              1 for success of the command (irrelevant)
     */
    public static int execute2D(CommandSourceStack sourceStack, int size, double temperature, int flips, BlockState spinUp, BlockState spinDown, boolean simulate) {
        Simulation2D simulation = flips == -1 ? new Simulation2D(size) : new Simulation2D(size, (int) Math.pow(10, flips));
        int[][] model = simulate ? simulation.simulate(temperature) : simulation.model;
        BlockPos pos = new BlockPos(sourceStack.getPosition());

        for (int i = 0; i < size; i++) {
            pos = pos.east(1); //east is positive x direction
            for (int j = 0; j < size; j++) {
                pos = pos.south(1); //south is positive z direction
                sourceStack.getLevel().setBlock(pos, model[i][j] == 1 ? spinUp : spinDown, Block.UPDATE_ALL);
            }
            pos = pos.south(-size);
        }

        return 1; //returning 1 means success for brigadier commands.
    }

    public static int execute3D(CommandSourceStack sourceStack, int size, double temperature, int flips, BlockState spinUp, BlockState spinDown, boolean simulate) {
        Simulation3D simulation = flips == -1 ? new Simulation3D(size) : new Simulation3D(size, (int) Math.pow(10, flips));
        int[][][] model = simulate ? simulation.simulate(temperature) : simulation.model;
        BlockPos pos = new BlockPos(sourceStack.getPosition()).below(1);

        for (int i = 0; i < size; i++) {
            pos = pos.east(1); //east is positive x direction
            for (int j = 0; j < size; j++) {
                pos = pos.south(1); //south is positive z direction
                for (int k = 0; k < size; k++) { //above is positive y direction
                    pos = pos.above(1);
                    sourceStack.getLevel().setBlock(pos, model[i][j][k] == 1 ? spinUp : spinDown, Block.UPDATE_ALL);
                }
                pos = pos.below(size);
            }
            pos = pos.south(-size);
        }

        return 1; //returning 1 means success for brigadier commands.
    }
}
