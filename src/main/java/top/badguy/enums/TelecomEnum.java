package top.badguy.enums;


public enum TelecomEnum {
    // telecom
    TELECOM("telecom"),
    // mobile
    MOBILE("mobile"),
    // unicom
    UNICOM("unicom");

    private final String value;

    TelecomEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
