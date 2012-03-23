package org.karatachi.portus.core.type;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public final class AccountRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Map<Integer, AccountRole> map =
            new TreeMap<Integer, AccountRole>();

    public static final AccountRole NONE = new AccountRole("なし", 0);

    public static final AccountRole DEFAULT = new AccountRole("一般ユーザ",
            Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.VIEW_FILE, Bit.MODIFY_FILE,
            Bit.VIEW_LOG);

    public static final AccountRole DEFAULT_LOG = new AccountRole("ログ取得一般ユーザ",
            Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.VIEW_FILE, Bit.MODIFY_FILE,
            Bit.VIEW_LOG, Bit.SAVE_LOG);

    public static final AccountRole ACCOUNT_ADMIN = new AccountRole(
            "アカウント管理権限", Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.MODIFY_ACCOUNT,
            Bit.VIEW_FILE, Bit.MODIFY_FILE, Bit.VIEW_LOG, Bit.SAVE_LOG);

    public static final AccountRole DOMAIN_ADMIN = new AccountRole("ドメイン管理権限",
            Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.MODIFY_ACCOUNT,
            Bit.MODIFY_DOMAIN, Bit.VIEW_FILE, Bit.MODIFY_FILE, Bit.VIEW_LOG,
            Bit.SAVE_LOG);

    public static final AccountRole CUSTOMER_ADMIN = new AccountRole("管理権限",
            Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.MODIFY_ACCOUNT,
            Bit.MODIFY_DOMAIN, Bit.MODIFY_CUSTOMER, Bit.VIEW_FILE,
            Bit.MODIFY_FILE, Bit.VIEW_LOG, Bit.SAVE_LOG);

    public static final AccountRole FULL_ADMIN = new AccountRole("全権限",
            Bit.VALIDUSER, Bit.CHANGE_PASS, Bit.MODIFY_ACCOUNT,
            Bit.MODIFY_DOMAIN, Bit.MODIFY_CUSTOMER, Bit.MODIFY_ALL,
            Bit.VIEW_FILE, Bit.MODIFY_FILE, Bit.VIEW_LOG, Bit.SAVE_LOG);

    public static final AccountRole DEVEL = new AccountRole("開発権限", -1);

    private String display;
    private int value;

    private AccountRole(String display, int value) {
        this.display = display;
        this.value = value;
        map.put(value, this);
    }

    private AccountRole(String display, Bit... bits) {
        this.display = display;
        for (Bit bit : bits) {
            value |= bit.value();
        }
        map.put(value, this);
    }

    public boolean hasBit(Bit bit) {
        return (value & bit.value()) != 0;
    }

    public boolean isSuperOf(AccountRole role) {
        return (value & role.value) == role.value && (value & ~role.value) != 0;
    }

    @Override
    public String toString() {
        return display;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccountRole)) {
            return false;
        }
        AccountRole role = (AccountRole) obj;
        return this.value == role.value;
    }

    public int value() {
        return value;
    }

    public static AccountRole valueOf(int value) {
        return map.get(value);
    }

    public static enum Bit {
        VALIDUSER(0), CHANGE_PASS(1), MODIFY_ACCOUNT(2), MODIFY_DOMAIN(3),
        MODIFY_CUSTOMER(4), VIEW_FILE(5), MODIFY_FILE(6), VIEW_LOG(7),
        SAVE_LOG(8), MODIFY_ALL(29), ROOT(30), DEVEL(31);

        private final int bit;

        private Bit(int bit) {
            this.bit = bit;
        }

        public int bit() {
            return bit;
        }

        public int value() {
            return 1 << bit;
        }
    }
}
