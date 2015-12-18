

/**
 * Equipmenttypes.
 * 
 * @author Sky
 *
 */
public enum EquipmentType {
	NO_SLOT(-1, "NONE"), 
	HEAD_SLOT(0, "HAT"), 
	CAPE_SLOT(1, "CAPE"), 
	AMULET_SLOT(2, "AMULET"), 
	WEAPON_SLOT(3, "WEAPON"), 
	CHEST_SLOT(4, "BODY"), 
	SHIELD_SLOT(5, "SHIELD"), 
	LEGS_SLOT(7, "LEGS"), 
	HANDS_SLOT(9, "HANDS"), 
	FEET_SLOT(10, "BOOTS"), 
	RING_SLOT(12, "RING"), 
	ARROWS_SLOT(13, "ARROW");

	private int value;
	private String name;

	private EquipmentType(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	public int getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	public static EquipmentType typeByName(String name) {
		for (EquipmentType type : values()) {
			if (type.name.equals(name)) {
				return type;
			}
		}
		return null;
	}
	
	public String toString() {
		return "value: " + value + ", name: " + name;
	}
}
