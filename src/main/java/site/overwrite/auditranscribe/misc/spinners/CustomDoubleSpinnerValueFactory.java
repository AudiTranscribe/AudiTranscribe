/*
 * CustomDoubleSpinnerValueFactory.java
 * Description: Custom double spinner value factory class.
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

package site.overwrite.auditranscribe.misc.spinners;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SpinnerValueFactory;
import site.overwrite.auditranscribe.utils.MathUtils;

import java.math.BigDecimal;

/**
 * Custom double spinner value factory class.
 * Adapted from <code>SpinnerValueFactory.DoubleSpinnerValueFactory</code>.
 */
public class CustomDoubleSpinnerValueFactory extends SpinnerValueFactory<Double> {
    // Attributes
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min") {
        @Override
        protected void invalidated() {
            Double currentValue = CustomDoubleSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            final double newMin = get();
            if (newMin > getMax()) {
                setMin(getMax());
                return;
            }

            if (currentValue < newMin) {
                CustomDoubleSpinnerValueFactory.this.setValue(newMin);
            }
        }
    };
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max") {
        @Override
        protected void invalidated() {
            Double currentValue = CustomDoubleSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            final double newMax = get();
            if (newMax < getMin()) {
                setMax(getMin());
                return;
            }

            if (currentValue > newMax) {
                CustomDoubleSpinnerValueFactory.this.setValue(newMax);
            }
        }
    };
    private final DoubleProperty amountToStepBy = new SimpleDoubleProperty(this, "amountToStepBy");

    private double lastValidValue;

    /**
     * Constructs a new CustomDoubleSpinnerValueFactory.
     *
     * @param min            The minimum allowed double value for the Spinner.
     * @param max            The maximum allowed double value for the Spinner.
     * @param initialValue   The value of the Spinner when first instantiated, must
     *                       be within the bounds of the min and max arguments, or
     *                       else the min value will be used.
     * @param amountToStepBy The amount to increment or decrement by, per step.
     * @param decimalPlaces  The number of decimal places to display.
     */
    public CustomDoubleSpinnerValueFactory(
            double min, double max, double initialValue, double amountToStepBy, int decimalPlaces
    ) {
        // Set up values
        setMin(min);
        setMax(max);
        setAmountToStepBy(amountToStepBy);

        // Set converter
        setConverter(new CustomDoubleStringConverter(decimalPlaces));

        // Update the value property listener
        valueProperty().addListener((o, oldValue, newValue) -> {
            // Check if the new value is null
            if (newValue == null) {
                // Set the value to the last valid value
                setValue(lastValidValue);
            } else {
                // When the value is set, we need to react to ensure it is a valid value (and if not, blow up
                // appropriately)
                double actualSetValue = newValue;
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

    public final void setMin(double value) {
        min.set(value);
    }

    public final double getMin() {
        return min.get();
    }

    public final void setMax(double value) {
        max.set(value);
    }

    public final double getMax() {
        return max.get();
    }

    public final void setAmountToStepBy(double value) {
        amountToStepBy.set(value);
    }

    public final double getAmountToStepBy() {
        return amountToStepBy.get();
    }

    // Overridden methods
    @Override
    public void decrement(int steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.subtract(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
        setValue(newValue.compareTo(minBigDecimal) >= 0 ? newValue.doubleValue() :
                (isWrapAround() ? MathUtils.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMin()));
    }

    @Override
    public void increment(int steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.add(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
        setValue(newValue.compareTo(maxBigDecimal) <= 0 ? newValue.doubleValue() :
                (isWrapAround() ? MathUtils.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMax()));
    }
}
