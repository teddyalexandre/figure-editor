package application.panels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import utils.IconFactory;

/**
 * A simple dialog providing an {@link Integer} value.
 * Can be used to change {@link history.HistoryManager} history size
 * @param <T> The type of {@link Number} managed by this size dialog
 * @author davidroussel
 * Code Example:
 * {@code
 * 	Dialog<Integer> sizeDialog = new SizeDialogPane<Integer>(...);
 * 	Optional<Integer> result = sizeDialog.showAndWait();
 * }
 */
public class SizeDialogPane<T extends Number> extends Dialog<T>
{
	/**
	 * The value Property that will be bound to UI spinner and used to return
	 * the value provided by this dialog.
	 */
	private ObjectProperty<T> valueProperty;

	/**
	 * Valued constructor
	 * @param value the current value to set in {@link #spinner}
	 * @param minValue  the minimum value to set in the {@link #spinner}
	 * @param maxValue the maximum value to set in the {@link #spinner}
	 * @param stepValue the step value to set in the {@link #spinner}
	 * @param title the title to set on this dialog
	 *
	 */
	public SizeDialogPane(T value,
	                      T minValue,
	                      T maxValue,
	                      T stepValue,
	                      String title)
	{
		super();
		valueProperty = new SimpleObjectProperty<T>(value);
		setTitle(title);
		setupUI(minValue, maxValue, stepValue);
		/*
		 * Set result converter to return #valueProperty's value if
		 * ButtonType.OK is pressed and null otherwise
		 */
		setResultConverter(new Callback<ButtonType, T>()
		{
			@Override
			public T call(ButtonType param)
			{
				if (param == ButtonType.OK)
				{
					return valueProperty.get();
				}
				else
				{
					return null;
				}
			}
		});
	}

	/**
	 * Setup the dialog UI.
	 * <ul>
	 * 	<li>Creates a {@link HBox}</li>
	 * 	<li>Creates a {@link Label}</li>
	 * 	<li>Evt adds an {@link ImageView} as the graphic of previous label with
	 * 	{@link IconFactory#getIcon(String)}</li>
	 * 	<li>Creates a {@link Spinner} with value from {@link #valueProperty},
	 * 	min value as 0, max value as 64 and step value as 1</li>
	 * 	<li>Binds {@link #valueProperty} to value property of the spinner</li>
	 * 	<li>The adds the label and spinner to the {@link HBox}</li>
	 * 	<li>Set {@link HBox} as content of dialog pane</li>
	 * 	<li>And finaly adds OK and CANCEL {@link ButtonType}s to dialog pane</li>
	 * <ul>
	 * @param minValue the minimum value for the spinner
	 * @param maxValue the maximum value for the spinner
	 * @param stepValue the step value for the spinner
	 * @throws ClassCastException if required type <T> is neither
	 * {@link Integer} nor {@link Double} because {@link SpinnerValueFactory} can
	 * only provide either {@link SpinnerValueFactory#IntegerSpinnerValueFactory}
	 * or {@link SpinnerValueFactory#DoubleSpinnerValueFactory}
	 * @throws ClassCastException if <T> is neither {@link Integer} nor
	 * {@link Double}
	 */
	@SuppressWarnings("unchecked") // Because of SpinnerValueFactory<T> casts
	private void setupUI(T minValue, T maxValue, T stepValue)
	{
		HBox content = new HBox();
		Label sizeLabel = new Label("Size: ");
		ImageView iconView = new ImageView(IconFactory.getIcon("clock"));
		iconView.setFitWidth(32);
		iconView.setFitHeight(32);
		sizeLabel.setGraphic(iconView);
		Spinner<T> spinner = new Spinner<T>();
		T value = valueProperty.get();
		if (value instanceof Integer)
		{
			spinner.setValueFactory((SpinnerValueFactory<T>)
				new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue.intValue(),
				                                                   maxValue.intValue(),
				                                                   value.intValue(),
				                                                   1));
		}
		else if (value instanceof Double)
		{
			spinner.setValueFactory((SpinnerValueFactory<T>)
				new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue.doubleValue(),
				                                                  maxValue.doubleValue(),
				                                                  value.doubleValue(),
				                                                  1.0));
		}
		else
		{
			throw new ClassCastException("Unmanaged type "
			    + value.getClass().getSimpleName());
		}
		valueProperty.bind(spinner.valueProperty());
		content.getChildren().addAll(sizeLabel, spinner);
		getDialogPane().setContent(content);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}
}
