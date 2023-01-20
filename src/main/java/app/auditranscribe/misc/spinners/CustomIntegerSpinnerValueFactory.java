/*
 * CustomIntegerSpinnerValueFactory.java
 * Description: Custom integer spinner value factory class.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.misc.spinners;

import app.auditranscribe.utils.MathUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Custom integer spinner value factory class.<br>
 * Adapted from <code>SpinnerValueFactory.IntegerSpinnerValueFactory</code>.
 */
public class CustomIntegerSpinnerValueFactory extends SpinnerValueFactory<Integer> {
    // Attributes
    private final IntegerProperty min = new SimpleIntegerProperty(this, "min") {
        @Override
        protected void invalidated() {
            Integer currentValue = CustomIntegerSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            int newMin = get();
            if (newMin > getMax()) {
                setMin(getMax());
                return;
            }

            if (currentValue < newMin) {
                CustomIntegerSpinnerValueFactory.this.setValue(newMin);
            }
        }
    };
    private final IntegerProperty max = new SimpleIntegerProperty(this, "max") {
        @Override
        protected void invalidated() {
            Integer currentValue = CustomIntegerSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            int newMax = get();
            if (newMax < getMin()) {
                setMax(getMin());
                return;
            }

            if (currentValue > newMax) {
                CustomIntegerSpinnerValueFactory.this.setValue(newMax);
            }
        }
    };
    private final IntegerProperty amountToStepBy = new SimpleIntegerProperty(this, "amountToStepBy");

    private int lastValidValue;

    // Constructor methods
    public CustomIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy) {
        // Set up values
        setMin(min);
        setMax(max);
        setAmountToStepBy(amountToStepBy);

        // Set converter
        setConverter(new CustomIntegerStringConverter());

        // Update the value property listener
        valueProperty().addListener((o, oldValue, newValue) -> {
            // Check if the new value is null
            if (newValue == null) {
                // Set the value to the last valid value
                setValue(lastValidValue);
            } else {
                // When the value is set, we need to react to ensure it is a valid value (and if not, blow up
                // appropriately)
                int actualSetValue = newValue;
                if (newValue < getMin()) {
                    actualSetValue = getMin();
                } else if (newValue > getMax()) {
                    actualSetValue = getMax();
                }
                setValue(actualSetValue);

                // Update the last valid value
                lastValidValue = actualSetValue;
            }
        });

        // Set initial value
        initialValue = initialValue >= min && initialValue <= max ? initialValue : min;
        setValue(initialValue);

        // Set last valid value
        lastValidValue = initialValue;
    }

    // Getter/Setter methods
    public final void setMin(int value) {
        min.set(value);
    }

    public final int getMin() {
        return min.get();
    }

    public final void setMax(int value) {
        max.set(value);
    }

    public final int getMax() {
        return max.get();
    }

    public final void setAmountToStepBy(int value) {
        amountToStepBy.set(value);
    }

    public final int getAmountToStepBy() {
        return amountToStepBy.get();
    }

    // Overridden methods
    @Override
    public void decrement(int steps) {
        final int min = getMin();
        final int max = getMax();
        final int newIndex = getValue() - steps * getAmountToStepBy();
        setValue(newIndex >= min ? newIndex : (isWrapAround() ? MathUtils.wrapValue(newIndex, min, max) + 1 : min));
    }

    @Override
    public void increment(int steps) {
        final int min = getMin();
        final int max = getMax();
        final int currentValue = getValue();
        final int newIndex = currentValue + steps * getAmountToStepBy();
        setValue(newIndex <= max ? newIndex : (isWrapAround() ? MathUtils.wrapValue(newIndex, min, max) - 1 : max));
    }
}