/*
 * CustomIntegerSpinnerValueFactory.java
 *
 * Created on 2022-06-17
 * Updated on 2022-06-17
 *
 * Description: Custom integer spinner value factory class.
 */

package site.overwrite.auditranscribe.misc.spinners;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.SpinnerValueFactory;
import site.overwrite.auditranscribe.utils.MathUtils;

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

    // Overwritten methods
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
