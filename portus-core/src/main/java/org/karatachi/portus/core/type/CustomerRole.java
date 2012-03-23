package org.karatachi.portus.core.type;

import java.io.Serializable;

public final class CustomerRole implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public static final CustomerRole DEFAULT = new CustomerRole(0);

    private int value;

    private CustomerRole(int value) {
        this.value = value;
    }

    public CustomerRole(Bit... bits) {
        for (Bit bit : bits) {
            value |= bit.value();
        }
    }

    public void addBit(Bit bit) {
        value |= bit.value();
    }

    public void removeBit(Bit bit) {
        value &= ~bit.value();
    }

    public boolean hasBit(Bit bit) {
        return (value & bit.value()) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CustomerRole)) {
            return false;
        }
        return this.value == ((CustomerRole) obj).value;
    }

    public int value() {
        return value;
    }

    public static CustomerRole valueOf(int value) {
        return new CustomerRole(value);
    }

    @Override
    public CustomerRole clone() {
        try {
            return (CustomerRole) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static enum Bit {
        LOGO(0), HASCHILD(1), STREAMING(4), AUTHORIZE(5);

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
