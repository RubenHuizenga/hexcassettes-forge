package com.proton.rubenhuizenga.hexcassettes.inits

import com.proton.rubenhuizenga.hexcassettes.PlayerMinterface
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel
import java.util.function.Supplier
import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import com.proton.rubenhuizenga.hexcassettes.client.ClientStorage
import io.netty.buffer.Unpooled

object HexcassettesNetworking {
    private const val PROTOCOL_VERSION = "1"
    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        HexcassettesMain.id("main"),
        { PROTOCOL_VERSION },
        { it == PROTOCOL_VERSION },
        { it == PROTOCOL_VERSION }
    )

    val CASSETTE_REMOVE: ResourceLocation = HexcassettesMain.id("cassette_remove")
    val SYNC_CASSETTES: ResourceLocation = HexcassettesMain.id("sync_cassettes")

    fun clientInit() {
        CHANNEL.messageBuilder(FriendlyByteBuf::class.java, 0)
            .encoder { _, _ -> } // No special encoding needed
            .decoder { buf -> buf } // Retain buffer for processing
            .consumerMainThread { buf, ctx -> handleSyncCassettes(buf, ctx) }
            .add()
    }

    fun init() {
        CHANNEL.messageBuilder(FriendlyByteBuf::class.java, 1)
            .encoder { _, _ -> } // No special encoding needed
            .decoder { buf -> buf } // Retain buffer for processing
            .consumerMainThread { buf, ctx -> handleCassetteRemove(buf, ctx) }
            .add()
    }

    private fun handleSyncCassettes(packet: FriendlyByteBuf, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            ClientStorage.ownedCassettes = packet.readInt()
            ClientStorage.activeCassettes.clear()
            val count = packet.readInt()
            repeat(count) {
                ClientStorage.activeCassettes.add(HexcassettesMain.deserializeKey(packet.readUtf()))
            }
            packet.release()
        }
        ctx.get().packetHandled = true
    }

    private fun handleCassetteRemove(packet: FriendlyByteBuf, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            (ctx.get().sender as PlayerMinterface).getCassetteState()
                .queuedHexes.remove(HexcassettesMain.deserializeKey(packet.readUtf()))
            packet.release()
        }
        ctx.get().packetHandled = true
    }

    fun sendToServer(id: ResourceLocation) {
        if (SYNC_CASSETTES == id) {
            val buf = FriendlyByteBuf(Unpooled.buffer())
            CHANNEL.send(PacketDistributor.SERVER.noArg(), buf)
        }
    }
}