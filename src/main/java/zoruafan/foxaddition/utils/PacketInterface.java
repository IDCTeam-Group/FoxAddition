package zoruafan.foxaddition.utils;

import zoruafan.foxaddition.utils.listeners.AbilitiesEvent;
import zoruafan.foxaddition.utils.listeners.BlockPlaceEvent;
import zoruafan.foxaddition.utils.listeners.EntityActionEvent;
import zoruafan.foxaddition.utils.listeners.HeldItemEvent;
import zoruafan.foxaddition.utils.listeners.InteractEntityEvent;
import zoruafan.foxaddition.utils.listeners.KeepAliveEvent;
import zoruafan.foxaddition.utils.listeners.PositionEvent;
import zoruafan.foxaddition.utils.listeners.SteerVehicleEvent;

public interface PacketInterface {
	void onAbilities(AbilitiesEvent abilities);
	void onBlockPlace(BlockPlaceEvent blockplace);
	void onHeldItem(HeldItemEvent helditem);
	void onKeetpAlive(KeepAliveEvent keepalive);
	void onPosition(PositionEvent position);
	void onInteractEntity(InteractEntityEvent interactentity);
	void onSteerVehicle(SteerVehicleEvent steervehicle);
	void onEntityAction(EntityActionEvent entityaction);
}