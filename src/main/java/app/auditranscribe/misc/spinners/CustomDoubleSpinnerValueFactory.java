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

package app.auditranscribe.misc.spinners;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SpinnerValueFactory;

import java.math.BigDecimal;

/**
 * Custom double spinner value factory class.
 * Adapted from <code>SpinnerValueFactory.DoubleSpinnerValueFactory</code>.
 */
@ExcludeFromGeneratedCoverageReport
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
     * Initializes a new <code>CustomDoubleSpinnerValueFactory</code>.
     *
     * @param min            The minimum allowed double value for the spinner.
     * @param max            The maximum allowed double value for the spinner.
     * @param initialValue   The value of the spinner when first instantiated. Must be within the
     *                       bounds of the <code>min</code> and <code>max</code> arguments, or else
     *                       the minimum value will be used.
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

    // Public methods
    @Override
    public void decrement(int steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.subtract(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
        setValue(newValue.compareTo(minBigDecimal) >= 0 ? newValue.doubleValue() :
                (isWrapAround() ? wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMin()));
    }

    @Override
    public void increment(int steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.add(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
        setValue(newValue.compareTo(maxBigDecimal) <= 0 ? newValue.doubleValue() :
                (isWrapAround() ? wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMax()));
    }

    // Private methods

    /**
     * Method that wraps a value to the maximum value if the value is smaller than the maximum value
     * and wraps to the minimum if the value is larger than the minimum value.
     *
     * @param value Value to wrap.
     * @param min   Minimum value.
     * @param max   Maximum value.
     * @return Wrapped value.
     * @throws ValueException If:<ul>
     *                        <li>
     *                        The maximum value is not a positive number.
     *                        </li>
     *                        <li>
     *                        The minimum value is larger than the maximum value.
     *                        </li>
     *                        </ul>
     */
    private BigDecimal wrapValue(BigDecimal value, BigDecimal min, BigDecimal max) {
        // Check if the values are valid
        if (max.doubleValue() <= 0) {
            throw new ValueException("The maximum value must be a positive number.");
        }

        if (min.compareTo(max) >= 0) {
            throw new ValueException("The minimum value must be smaller than to the maximum value.");
        }

        // Perform actual computation
        if (value.compareTo(min) < 0) {
            return max;
        } else if (value.compareTo(max) > 0) {
            return min;
        }
        return value;
    }
}
