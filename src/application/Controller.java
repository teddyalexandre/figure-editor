package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;


import application.cells.FigureCell;
import application.cells.FigureTypeCell;
import application.cells.LineTypeCell;
import application.panels.InfoPanelController;
import application.panels.SizeDialogPane;
import figures.Drawing;
import figures.Figure;
import figures.enums.FigureType;
import figures.enums.LineType;
import figures.filters.CompositeFigureFilter;
import figures.filters.FigureFilters;
import figures.filters.FigureTypeFilter;
import figures.filters.EdgeColorFilter;
import figures.filters.FillColorFilter;
import figures.filters.LineTypeFilter;
import figures.filters.LineWidthFilter;
import history.HistoryManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logger.LoggerFactory;
import tools.AbstractTool;
import tools.CursorTool;
import tools.SelectionTool;
import tools.TransformTool;
import utils.IconFactory;

/**
 * Controller associated with EditorFrame.fxml
 * Contains:
 * <ul>
 * 	<li>FXML UI elements that need to be referenced in business logic</li>
 * 	<li>onXXXX() callback methods handling UI requests</li>
 * </ul>
 * @author davidroussel
 * @see Initializable so it can initialize FXML related attributes.
 */
public class Controller implements Initializable
{
	// -------------------------------------------------------------------------
	// internal attributes
	// -------------------------------------------------------------------------
	/**
	 * Logger to show debug message or only log them in a file
	 */
	protected Logger logger = null;

	/**
	 * Reference to parent stage so it can be quickly closed on quit
	 * Initialized through {@link #setParentStage(Stage)} in
	 * {@link Main#start(Stage)}
	 */
	private Stage parentStage = null;

	/**
	 * Drawing model containing {@link Figure}s and
	 * <ul>
	 * 	<li>drawn in {@link #drawingPane}</li>
	 * 	<li>showed in {@link #figuresListView}
	 * </ul>
	 */
	private Drawing drawingModel = null;

	/**
	 * Inclusive Composite {@link Figure}s filter to filter Figures based on
	 * their {@link FigureType}s.
	 * If one of the filters in {@link #figureTypesFilter} returns true then
	 * the composite filter returns true.
	 */
	private FigureFilters<FigureType> figureTypesFilter;

	/**
	 * Exclusive Composite {@link Figure}s filter to fitler figures based on
	 * other filters. Such as
	 * <ul>
	 * 	<li>#figureTypesFilter</li>
	 * 	<li>Figure edgeColor</li>
	 * 	<li>Figure fillColor</li>
	 * 	<li>figures.enums.LineType</li>
	 * 	<li>Figure line width</li>
	 * </ul>
	 * If all of the filters in {@link #figuresFilter} returned true then the
	 * composite filter returns true.
	 */
	private CompositeFigureFilter figuresFilter;

	/**
	 * A Simple Boolean property indicating figures are filtered.
	 * To be bound to {@link #filterToggleButton}
	 * @see #filterToggleButton
	 * @see #filterToggleCheckMenuItem
	 */
	private BooleanProperty filteringProperty;

	/**
	 * History Manager to manage Undo / Redos on {@link #drawingModel}
	 */
	private HistoryManager<Figure> historyManager = null;

	/**
	 * Current Tool attached to {@link #drawingPane} and {@link #drawingModel}
	 */
	private AbstractTool<Pane> currentTool = null;

	/**
	 * Tool to move / rotate / scale figures (in edit mode only)
	 */
	private AbstractTool<Pane> transformTool = null;

	/**
	 * CursorTool to update mouse coordinates in {@link #drawingPane} using
	 * {@link #cursorXLabel} and {@link #cursorYLabel} at the bottom right of
	 * the scene.
	 */
	private CursorTool cursorTool = null;

	// -------------------------------------------------------------------------
	// FXML identified attributes (with fx:id)
	// -------------------------------------------------------------------------
	/**
	 * Application menu bar
	 */
	@FXML
	private MenuBar menuBar;

	/**
	 * Application Drawing Pane
	 */
	@FXML
	private Pane drawingPane;

	/**
	 * Toolbar "MoveDown" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button undoButton;

	/**
	 * Toolbar "Redo" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button redoButton;

	/**
	 * Toolbar "Clear" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button clearButton;

	/**
	 * Toolbar "MoveDown" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 * @see #editToggleImageView
	 */
	@FXML
	private ToggleButton editToggleButton;

	/**
	 * Image contained if {@link #editToggleButton} so it can be changed
	 * when the button is toggled
	 * @see #editIcon
	 * @see #createIcon
	 * @see #onEditAction(ActionEvent)
	 */
	@FXML
	private ImageView editToggleImageView;

	/**
	 * "MoveDown" Image to display in Creation mode
	 * @see #editToggleImageView
	 * @see #onEditAction(ActionEvent)
	 */
	private final static Image editIcon = IconFactory.getIcon("create_new");

	/**
	 * "Create" Image to display in MoveDown mode
	 * @see #editToggleImageView
	 * @see #onEditAction(ActionEvent)
	 */
	private final static Image createIcon = IconFactory.getIcon("edit");

	/**
	 * Toolbar "Delete (selected)" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button deleteButton;

	/**
	 * Toolbar "Move Up" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button moveUpButton;

	/**
	 * Toolbar "Move Down" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button moveDownButton;

	/**
	 * Toolbar "Move on Top" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button moveTopButton;

	/**
	 * Toolbar Move to Bottom Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button moveBottomButton;

	/**
	 * Toolbar "Apply Style" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button applyStyleButton;

	/**
	 * Toolbar "Filtering" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 * @see #filterToggleCheckMenuItem as they should be bound together
	 */
	@FXML
	private ToggleButton filterToggleButton;

	/**
	 * ImageView contained in {@link #filterToggleButton} so it can be changed
	 * with either {@link #emptyFilterImage} of {@link #filledFilterImage}
	 * @see #onFilterAction(ActionEvent)
	 */
	@FXML
	private ImageView filterToggleImageView;

	/**
	 * Image to show in {@link #filterToggleImageView} when filtering is on in
	 * order to turn it off
	 * @see #filterToggleButton
	 * @see #filterToggleImageView
	 * @see #onFilterAction(ActionEvent)
	 */
	private Image emptyFilterImage = IconFactory.getIcon("empty_filter");

	/**
	 * Image to show in {@link #filterToggleImageView} when filtering is off in
	 * order to turn it on
	 * @see #filterToggleButton
	 * @see #filterToggleImageView
	 * @see #onFilterAction(ActionEvent)
	 */
	private Image filledFilterImage = IconFactory.getIcon("filled_filter");
	
	/**
	 * CheckMenuItem to toggle figures filtering
	 * @see #filterToggleButton as they should be bound together
	 */
	@FXML
	private CheckMenuItem filterBySelectedCheckMenuItem;

	/**
	 * CheckMenuItem to toggle figures filtering
	 * @see #filterToggleButton as they should be bound together
	 */
	@FXML
	private CheckMenuItem filterToggleCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.Circle} figures
	 */
	@FXML
	private CheckMenuItem filterCirclesCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.Ellipse} figures
	 */
	@FXML
	private CheckMenuItem filterEllipsesCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.Rectangle} figures
	 */
	@FXML
	private CheckMenuItem filterRectanglesCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.RoundedRectangle} figures
	 */
	@FXML
	private CheckMenuItem filterRoundedRectanglesCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.Polygon} figures
	 */
	@FXML
	private CheckMenuItem filterPolygonsCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.NGon} figures
	 */
	@FXML
	private CheckMenuItem filterNGonsCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of {@link figures.Star} figures
	 */
	@FXML
	private CheckMenuItem filterStarsCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of figures with the current Fill Color
	 */
	@FXML
	private CheckMenuItem filterFillColorCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of figures with the current Edge Color
	 */
	@FXML
	private CheckMenuItem filterEdgeColorCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of figures with the current Line Type
	 */
	@FXML
	private CheckMenuItem filterLineTypeCheckMenuItem;

	/**
	 * CheckMenuItem to toggle filtering of figures with the current Line Width
	 */
	@FXML
	private CheckMenuItem filterLineWidthCheckMenuItem;

	/**
	 * Toolbar "Quit" Button
	 * @implSpec Should be part of {@link #styleableButtons}
	 */
	@FXML
	private Button quitButton;

	/**
	 * Shape types Combobox
	 */
	@FXML
	private ComboBox<FigureType> shapeTypeComboBox;

	/**
	 * CheckBox indicating Fill Color should be taken into account
	 */
	@FXML
	private CheckBox useFillColor;

	/**
	 * Color Picker to chose Fill Color
	 */
	@FXML
	private ColorPicker fillColorPicker;

	/**
	 * CheckBox indicating Edge Color should be taken into account
	 */
	@FXML
	private CheckBox useEdgeColor;

	/**
	 * Color Picker to chose Edge Color
	 */
	@FXML
	private ColorPicker edgeColorPicker;

	/**
	 * Line types Combobox
	 */
	@FXML
	private ComboBox<LineType> lineTypeCombobox;

	/**
	 * Line width Spinner
	 */
	@FXML
	private Spinner<Double> lineWidthSpinner;

	/**
	 * Figures ListView
	 */
	@FXML
	private ListView<Figure> figuresListView;

	/**
	 * Label to display info messages (such as tips on how to proceed)
	 */
	@FXML
	private Label messagesLabel;

	/**
	 * Label showing cursor X coordinate in {@link #drawingPane}
	 */
	@FXML
	private Label cursorXLabel;

	/**
	 * Label showing cursor Y coordinate in {@link #drawingPane}
	 */
	@FXML
	private Label cursorYLabel;

	/**
	 * The Info Panel loaded from ./panels/InfoPanel.fxml
	 */
	@FXML
	private GridPane infoPanel;

	/**
	 * The Controller of {@link #infoPanel}
	 */
	@FXML
	private InfoPanelController infoPanelController;

	// -------------------------------------------------------------------------
	// Other FXML attributes
	// -------------------------------------------------------------------------

	/**
	 * List of buttons with display style that can change.
	 * These buttons are:
	 * <ul>
	 * 	<li>{@link #undoButton}</li>
	 * 	<li>{@link #redoButton}</li>
	 * 	<li>{@link #clearButton}</li>
	 * 	<li>{@link #editToggleButton}</li>
	 * 	<li>{@link #deleteButton}</li>
	 * 	<li>{@link #moveUpButton}</li>
	 * 	<li>{@link #moveDownButton}</li>
	 * 	<li>{@link #moveTopButton}</li>
	 * 	<li>{@link #moveBottomButton}</li>
	 * 	<li>{@link #applyStyleButton}</li>
	 * 	<li>{@link #filterToggleButton}</li>
	 * 	<li>{@link #quitButton}</li>
	 * </ul>
	 */
	private List<Labeled> styleableButtons;

	/**
	 * Default constructor.
	 * Initialize all non FXML attributes
	 * @see ModifiableObservableList
	 */
	public Controller()
	{
		// --------------------------------------------------------------------
		// Initialize own attributes
		// --------------------------------------------------------------------
		figureTypesFilter = new FigureFilters<FigureType>();	// inclusive
		figuresFilter = new CompositeFigureFilter(true);		// exclusive
		figuresFilter.add(figureTypesFilter);
		filteringProperty = new SimpleBooleanProperty();

		/*
		 * Can't get parent logger now, so standalone logger.
		 * Parent logger will be set in Main.
		 */
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       null,
		                                       Level.INFO);
	}

	/**
	 * Controller initialization to initialize FXML related attributes.
	 * @param location The location used to resolve relative paths for the root
	 * object, or null if the location is not known.
	 * @param resources The resources used to localize the root object, or null
	 * if the root object was not localized.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		/*
		 * DONE Controller#initialize: setting up #drawingModel
		 * with
		 * 	- #drawingPane
		 * 	- #figuresListView
		 * 	- #logger
		 */
		drawingModel = new Drawing(drawingPane,figuresListView,logger);

		/*
		 * DONE Controller#initialize: Binds properties of UI elements to #drawingModel so that changes
		 * in those UI elements will directly be reflected in properties of the
		 * #drawingModel without callbacks:
		 * 	- #shapeTypeComboBox --> Drawing#figureTypeProperty
		 * 	- #useFillColor --> Drawing#hasFillColorProperty
		 * 	- #fillColorPicker --> Drawing#fillColorProperty
		 * 	- #useEdgeColor --> Drawing#hasEdgeColorProperty
		 * 	- #edgeColorPicker --> Drawing#edgeColorProperty
		 * 	- #lineTypeCombobox --> Drawing#lineTypeProperty
		 * 	- #lineWidthSpinner --> Drawing#lineWidthProperty
		 */
		drawingModel.bindFigureTypeProperty(shapeTypeComboBox.valueProperty());
		drawingModel.bindHasFillColorProperty(useFillColor.selectedProperty());
		drawingModel.bindFillColorProperty(fillColorPicker.valueProperty());
		drawingModel.bindHasEdgeColorProperty(useEdgeColor.selectedProperty());
		drawingModel.bindEdgeColorProperty(edgeColorPicker.valueProperty());
		drawingModel.bindLineTypeProperty(lineTypeCombobox.valueProperty());
		drawingModel.bindLineWidthProperty(lineWidthSpinner.valueProperty());

		/*
		 * DONE Controller#initialize: Setting up #historyManager with
		 * 	- #drawingModel
		 * 	- 32 Undo / Redo steps (changeable in #onSetHistorySizeAction)
		 * 	- #logger
		 */
		historyManager = new HistoryManager<Figure>(drawingModel,32,logger);

		/*
		 * DONE Controller#initialize Setup #figureTypesFilter and  #figuresFilter
		 * according to selected states of
		 * 	- #filterCirclesCheckMenuItem
		 * 	- #filterRectanglesCheckMenuItem
		 * 	- #filterRoundedRectanglesCheckMenuItem
		 * 	- #filterPolygonsCheckMenuItem
		 * 	- #filterNGonsCheckMenuItem
		 * 	- #filterStarsCheckMenuItem
		 * 	- #filterFillColorCheckMenuItem
		 * 	- #filterEdgeColorCheckMenuItem
		 * 	- #filterLineTypeCheckMenuItem
		 * 	- #filterLineWidthCheckMenuItem
		 */
		if(filterCirclesCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.CIRCLE));
		}
		if(filterEllipsesCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.ELLIPSE));
		}
		if(filterRectanglesCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.RECTANGLE));
		}
		if(filterRoundedRectanglesCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.ROUNDED_RECTANGLE));
		}
		if(filterPolygonsCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.POLYGON));
		}
		if(filterNGonsCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.NGON));
		}
		if(filterStarsCheckMenuItem.isSelected()) {
			figureTypesFilter.add(new FigureTypeFilter(FigureType.STAR));
		}
		
		if(filterFillColorCheckMenuItem.isSelected()) {
			figuresFilter.add(new FillColorFilter(drawingModel.getFillColor()));
		}
		if(filterEdgeColorCheckMenuItem.isSelected()) {
			figuresFilter.add(new EdgeColorFilter(drawingModel.getEdgeColor()));
		}
		if(filterLineTypeCheckMenuItem.isSelected()) {
			figuresFilter.add(new LineTypeFilter(drawingModel.getLineType()));
		}
		if(filterLineWidthCheckMenuItem.isSelected()) {
			figuresFilter.add(new LineWidthFilter(drawingModel.getLineWidth()));
		}
		//figureTypesFilter.add();
		// --------------------------------------------------------------------
		// Initialize FXML related attributes
		// --------------------------------------------------------------------

		/*
		 * DONE Setup #shapeTypeComboBox with
		 * 	- items as all FigureTypes
		 * 	- value as FigureType.CIRCLE
		 * DONE Controller#initialize: If you have provided a FigureTypeCell with its controller,
		 * then setup
		 * 	- ButtonCell as new FigureTypeCell()
		 * 	- CellFactory as combobox -> new FigureTypeCell()
		 */
		shapeTypeComboBox.getItems().addAll(FigureType.all());
		shapeTypeComboBox.setValue(FigureType.CIRCLE);
		shapeTypeComboBox.setButtonCell(new FigureTypeCell());
		shapeTypeComboBox.setCellFactory(combobox -> new FigureTypeCell());
		

		/*
		 * DONE Setup #lineTypeCombobox with
		 * 	- items as all LineTypes
		 * 	- value as LineType.SOLID
		 * DONE Controller#initialize:  If you have provided a LineTypeCell CustomCell and it controller
		 * then setup
		 * 	- ButtonCell as new LineTypeCell()
		 * 	- CellFactory as combobox -> new LineTypeCell()
		 */
		lineTypeCombobox.getItems().addAll(LineType.all());
		lineTypeCombobox.setValue(LineType.SOLID);
		lineTypeCombobox.setButtonCell(new LineTypeCell());
		lineTypeCombobox.setCellFactory(combobox -> new LineTypeCell());


		/*
		 * DONE Controller#initialize: Setup #useFillColor, #useEdgeColor, #fillColorPicker and #edgeColorPicker
		 * in a consistent state :
		 * 	- if #useFillColor is deselected
		 * 		- #fillColorPicker should be disabled
		 * 		- #useEdgeColor should be selected
		 *	- if #useEdgeColor is deselected
		 *		- #edgeColorPicker should be disabled
		 *		- #useFillColor should be selected
		 * dynamic check should be performed in #onCheckColorsConsistencyAction
		 */
		if (!useFillColor.isSelected()) {
			fillColorPicker.setDisable(true);
			useEdgeColor.setSelected(true);
		}
		if (!useEdgeColor.isSelected()) {
			edgeColorPicker.setDisable(true);
			useFillColor.setSelected(true);
		}

		/*
		 * DONE Controller#initialize: Setup #lineWidthSpinner with an new SimpleValueFactory
		 * 	- ranging from 1.0 to 32.0
		 * 	- current value 2.0
		 * 	- step value 1.0
		 * see SpinnerValueFactory
		 */
		lineWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0,32.0,2.0,1));


		/*
		 * DONE Controller#initialize: Setup #figuresListView with
		 * 	- content from #drawingModel
		 * 	- CellFactory as FigureCell
		 * Note: #figuresListView has already been set up in Drawing constructor
		 * with:
		 * 	- multiple selections
		 * 	- #drawingModel as a ListChangeListener to #figuresListView
		 */
		figuresListView.setItems(drawingModel);
		figuresListView.setCellFactory(data -> new FigureCell());

		/*
		 * DONE Controller#initialize: Setup #messagesLabel with empty or null message
		 */
		messagesLabel.setText("");

		/*
		 * DONE Controller#initialize: Bind #filterToggleButton, #filterToggleCheckMenuItem and
		 * #filteringProperty properties so that when one changes the others
		 * also changes.
		 * 	- selectedProperty
		 * 	- disableProperty
		 */
		filterToggleButton.selectedProperty().bindBidirectional(this.filteringProperty);
		filterToggleCheckMenuItem.selectedProperty().bindBidirectional(this.filteringProperty);
		filterToggleButton.disableProperty().bindBidirectional(this.filterToggleCheckMenuItem.disableProperty());
		

		/*
		 * DONE Controller#initialize: Disable Edit mode buttons until edit mode is on:
		 * 	- #deleteButton
		 * 	- #moveUpButton
		 * 	- #moveDownButton
		 * 	- #moveTopButton
		 * 	- #moveBottomButton
		 * 	- #applyStyleButton
		 * 	- #filterToggleButton
		 */
		deleteButton.setDisable(true);
		moveUpButton.setDisable(true);
		moveDownButton.setDisable(true);
		moveTopButton.setDisable(true);
		moveBottomButton.setDisable(true);
		applyStyleButton.setDisable(true);
		filterToggleButton.setDisable(true);

		/*
		 * Create #styleableButtons so they can be style updated in
		 * 	- #onDisplayButtonsWithGraphicsOnlyAction
		 * 	- #onDisplayButtonsWithTextOnlyAction
		 * 	- #onDisplayButtonsWithTextAndGraphicsAction
		 */
		styleableButtons = new ArrayList<Labeled>();
		styleableButtons.add(undoButton);
		styleableButtons.add(redoButton);
		styleableButtons.add(clearButton);
		styleableButtons.add(editToggleButton);
		styleableButtons.add(deleteButton);
		styleableButtons.add(moveUpButton);
		styleableButtons.add(moveDownButton);
		styleableButtons.add(moveTopButton);
		styleableButtons.add(moveBottomButton);
		styleableButtons.add(applyStyleButton);
		styleableButtons.add(filterToggleButton);
		styleableButtons.add(quitButton);

		/*
		 * Setup #infoPanelController with
		 * 	- #drawingPane
		 * 	- #drawingModel
		 * 	- #logger
		 */
		infoPanelController.setup(drawingPane, drawingModel, logger);

		/*
		 * Creates the #cursorTool as an EventHandler of the #drawingPane
		 */
		cursorTool = new CursorTool(drawingPane, cursorXLabel, cursorYLabel, logger);

		/*
		 * Create current tool (by default: creation tool)
		 */
		setTools(false);
	}

	/**
	 * Sets parent logger
	 * @param logger the new parent logger
	 */
	public void setParentLogger(Logger logger)
	{
		this.logger.setParent(logger);
	}

	/**
	 * Set parent stage (so it can be closed on quit)
	 * @param stage the new parent stage to set
	 */
	public void setParentStage(Stage stage)
	{
		parentStage = stage;
	}

	/**
	 * Action to Undo the last operation
	 * @param event event associated with this action
	 */
	@FXML
	public void onUndoAction(ActionEvent event)
	{
		logger.info("Undo Action triggered");
		// DONE Controller#onUndoAction ...
		historyManager.undo();
	}

	/**
	 * Action to Redo the last operation
	 * @param event event associated with this action
	 */
	@FXML
	public void onRedoAction(ActionEvent event)
	{
		logger.info("Redo Action triggered");
		// DONE Controller#onRedoAction ...
		historyManager.redo();
	}

	/**
	 * Action to Clear all figures in {@link #drawingModel}
	 * @param event event associated with this action
	 */
	@FXML
	public void onClearAction(ActionEvent event)
	{
		logger.info("Clear Action triggered");
		// DONE Controller#onClearAction
		drawingModel.clear();
	}

	/**
	 * Action to toggle Edit mode (Create mode <--> Edit mode)
	 * @param event event associated with this action
	 * @implSpec {@link #editToggleImageView} can be changed with either
	 * {@link #editIcon} {@link #createIcon} depending on
	 * {@link #editToggleButton} selected state
	 * @see #applyOnOffIcons(Toggle, ImageView, Image, Image)
	 */
	@FXML
	public void onEditAction(ActionEvent event)
	{
		logger.info("Edit Action triggered");
		Object source = event.getSource();
		boolean selected = false;
		/*
		 * DONE Controller#onEditAction ...
		 * 	- setup selected from source (ToggleButton or CheckMenuItem)
		 */
		if (source instanceof ToggleButton) {
			selected = ((ToggleButton) source).isSelected();
		}
		else if (source instanceof CheckMenuItem) {
			selected = ((CheckMenuItem) source).isSelected();
		}
		/*
		 * DONE Controller#onEditAction: Set Tools according to selected
		 */
		setTools(selected);

		/*
		 * DONE Controller#onEditAction: if creation mode then turn off filtering
		 * 	- uncheck #filterToggleButton when editing is off
		 * 	- calls onFilterAction
		 */
		if(!selected) {
			filterToggleButton.setSelected(false);
			onFilterAction(event);
		}
		

		/*
		 * DONE Controller#onEditAction: Enable / Disable edit mode buttons
		 * 	- #deleteButton
		 * 	- #moveUpButton
		 * 	- #moveDownButton
		 * 	- #moveTopButton
		 * 	- #moveBottomButton
		 * 	- #applyStyleButton
		 * 	- #filterToggleButton
		 */
		deleteButton.setDisable(!deleteButton.isDisable());
		moveUpButton.setDisable(!moveUpButton.isDisable());
		moveDownButton.setDisable(!moveDownButton.isDisable());
		moveTopButton.setDisable(!moveTopButton.isDisable());
		moveBottomButton.setDisable(!moveBottomButton.isDisable());
		applyStyleButton.setDisable(!applyStyleButton.isDisable());
		filterToggleButton.setDisable(!filterToggleButton.isDisable());
	}

	/**
	 * Creates the appropriate tool(s) to listen to {@link MouseEvent}s on the
	 * {@link #drawingPane} depending on editMode
	 * <ul>
	 * 	<li>if editMode is on then register edit tools (selection, move, rotate, scale)</li>
	 * 	<li>if editMode is off then register creation tool (to create new figures)</li>
	 * </ul>
	 * @param editMode the mode to be selected
	 */
	protected void setTools(boolean editMode)
	{
		/*
		 * DONE Controller#setTools ...
		 * 	- unregister #currentTool & #transformTool
		 * 	- if edit mode
		 * 		- setup SelectionTool as currentTool
		 * 		- setup transformTool
		 * 	- if creation mode
		 * 		- setup currentTool with the appropriate tool provided by
		 * 		FigureType#getCreationTool
		 */
		if (currentTool!=null) {
			currentTool.unregister();
		}
		if (transformTool!=null) {
			transformTool.unregister();
		}
		currentTool=null;
		transformTool=null;
		
		if(editMode) {
			currentTool = new SelectionTool(drawingPane,drawingModel,logger);
			transformTool = new TransformTool(drawingPane,drawingModel,messagesLabel,historyManager,logger);
		} else {
			currentTool = drawingModel.getFigureType().getCreationTool(
					drawingPane,
					drawingModel,
                    messagesLabel,
                    historyManager,
                    logger);
		}

		logger.info("Current tool = " + currentTool);
	}

	/**
	 * Action to delete selected figures (in {@link #drawingModel})
	 * @param event event associated with this action
	 */

	private List<Figure> getSelectedFigures() {
		return figuresListView.getSelectionModel().getSelectedItems();
	}

	private List<Integer> getSelectedFiguresIndices() {
		return figuresListView.getSelectionModel().getSelectedIndices();
	}

	private void setSelectedFiguresIndices(List<Integer> indices) {
		figuresListView.getSelectionModel().getSelectedIndices().clear();
		figuresListView.getSelectionModel().getSelectedIndices().addAll(indices);
	}
	@FXML
	public void onDeleteSelectedAction(ActionEvent event)
	{
		logger.info("Delete Selected Action triggered");
		/*
		 * DONE Controller#onDeleteSelectedAction ...
		 * Retrieve Selected figures in #view and delete them in reverse order
		 * to preserve valid indices provided by the selected list
		 */
		if (this.getSelectedFigures().isEmpty()) return;
		this.drawingModel.remove((Figure) this.getSelectedFigures().get(this.getSelectedFigures().size()-1));
	}

	/**
	 * Action to Move selected figures Up (down in {@link #drawingModel})
	 * @param event event associated with this action
	 */
	@FXML
	public void onMoveUpAction(ActionEvent event)
	{
		logger.info("MoveUp Action triggered");
		// DONE Controller#onMoveUpAction ...
		if (this.getSelectedFigures().isEmpty()) return;
		Figure selected = this.getSelectedFigures().get(this.getSelectedFigures().size()-1);
		int index = this.drawingModel.indexOf(selected);
		if (index - 1 >= 0) {
			this.drawingModel.remove(selected);
			this.drawingModel.add(index-1, selected);
			this.figuresListView.getSelectionModel().clearAndSelect(index-1);
		}
	}

	/**
	 * Action to Move selected figures Down (up in {@link #drawingModel})
	 * @param event event associated with this action
	 */
	@FXML
	public void onMoveDownAction(ActionEvent event)
	{
		logger.info("MoveDown Action triggered");
		// DONE Controller#onMoveDownAction ...
		if (this.getSelectedFigures().isEmpty()) return;
		Figure selected = this.getSelectedFigures().get(this.getSelectedFigures().size()-1);
		int index = this.drawingModel.indexOf(selected);
		if (index + 1 < this.drawingModel.size()) {
			this.drawingModel.remove(selected);
			this.drawingModel.add(index+1, selected);
			this.figuresListView.getSelectionModel().clearAndSelect(index+1);
		}
	}

	/**
	 * Action to Move selected figures on Top (after all other figures in {@link #drawingModel})
	 * @param event event associated with this action
	 */
	@FXML
	public void onMoveTopAction(ActionEvent event)
	{
		logger.info("MoveTop Action triggered");
		// DONE Controller#onMoveTopAction ...
		if (this.getSelectedFigures().isEmpty()) return;
		Figure selected = this.getSelectedFigures().get(this.getSelectedFigures().size()-1);
		this.drawingModel.remove(selected);
		this.drawingModel.add(0, selected);
		this.figuresListView.getSelectionModel().clearAndSelect(0);
	}

	/**
	 * Action to Move selected figures to Bottom (before all other figures in {@link #drawingModel})
	 * @param event event associated with this action
	 */
	@FXML
	public void onMoveBottomAction(ActionEvent event)
	{
		logger.info("MoveBottom Action triggered");
		// DONE Controller#onMoveBottomAction ...
		if (this.getSelectedFigures().isEmpty()) return;
		Figure selected = this.getSelectedFigures().get(this.getSelectedFigures().size()-1);
		this.drawingModel.remove(selected);
		int size = this.drawingModel.size();
		this.drawingModel.add(size, selected);
		this.figuresListView.getSelectionModel().clearAndSelect(size);
	}

	/**
	 * Action to Apply the current Style (Fill & Stroke Colors, Line Type and
	 * Line Width) to all selected figures in {@link #drawingModel}
	 * @param event event associated with this action
	 */
	@FXML
	public void onApplyStyleAction(ActionEvent event)
	{
		logger.info("ApplyStyle Action triggered");
		// DONE Controller#onApplyStyleAction ...
		for (Figure f : this.drawingModel) {
			if (f.isSelected()) {
				f.setFillColor(drawingModel.getFillColor());
				f.setEdgeColor(drawingModel.getEdgeColor());
				f.setLineType(drawingModel.getLineType());
				f.setLineWidth(drawingModel.getLineWidth());
			}
		}
	}

	/**
	 * Action to toggle Filtering of figures in {@link #drawingModel}
	 * @param event event associated with this action
	 * @implNote This action can also be triggered by
	 * {@link #onEditAction(ActionEvent)} in order to turn filtering off when
	 * editing is off
	 */
	@FXML
	public void onFilterAction(ActionEvent event)
	{
		logger.info("Filter Action triggered");
		// DONE Do we really need this callback ? Can't we bind properties instead ???
		boolean selected = this.filteringProperty.get();
		/*
		 * DONE Controller#onFilterAction: setup selected from source
		 */
		if(selected) {
			filterToggleImageView.setImage(filledFilterImage);
		} else {
			filterToggleImageView.setImage(emptyFilterImage);
		}
		

		/*
		 * TODO Controller#onFilterAction: Replace #drawingModel in #figuresListView with
		 * ObservableList#filtered(#figuresFilter) if selected, otherwise
		 * re-set #drawingModel as content
		 */
		if(selected) {
			figuresListView.setItems(drawingModel.filtered(figuresFilter));
		} else {
			drawingModel.refresh();
			figuresListView.setItems(drawingModel);
		}
		
	}

	/**
	 * Generic Action to toggle one of the filters to apply on {@link #drawingModel}.
	 * Since there is 7 Shape filters, 2 color filters and 2 line filters,
	 * this single callback will handle all filters based on
	 * {@link ActionEvent#getSource()}
	 * @param event event associated with this action
	 */
	@FXML
	public void onFilterChangedAction(ActionEvent event)
	{
		Object source = event.getSource();
		logger.info("Filter Change Action triggered from " + source);
		if (!(source instanceof CheckMenuItem))
		{
			logger.warning("event source is not a CheckMenuItem");
			return;
		}
		CheckMenuItem item = (CheckMenuItem) source;
		boolean selected = item.isSelected();

		FigureType type = FigureType.CIRCLE;
		if (item == filterCirclesCheckMenuItem)
		{
			type = FigureType.CIRCLE;
		}
		if (item == filterEllipsesCheckMenuItem)
		{
			type = FigureType.ELLIPSE;
		}
		if (item == filterRectanglesCheckMenuItem)
		{
			type = FigureType.RECTANGLE;
		}
		if (item == filterRoundedRectanglesCheckMenuItem)
		{
			type = FigureType.ROUNDED_RECTANGLE;
		}
		if (item == filterPolygonsCheckMenuItem)
		{
			type = FigureType.POLYGON;
		}
		if (item == filterNGonsCheckMenuItem)
		{
			type = FigureType.NGON;
		}
		if (item == filterStarsCheckMenuItem)
		{
			type = FigureType.STAR;
		}

		if (selected)
		{
			figureTypesFilter.add(new FigureTypeFilter(type));
		}
		else
		{
			figureTypesFilter.removeFilterWith(type);
		}

		if (item == filterFillColorCheckMenuItem)
		{
			Color color = drawingModel.getFillColor();
			if (selected)
			{
				// DONE Controller#onFilterChangedAction: add FillColorFilter to figuresFilter ...
				figuresFilter.add(new FillColorFilter(color));
			}
			else
			{
				// DONE Controller#onFilterChangedAction: remove any filter containing color from figuresFilter ...
				figuresFilter.removeFilterWith(color);
			}
		}

		if (item == filterEdgeColorCheckMenuItem)
		{
			Color color = drawingModel.getEdgeColor();
			if (selected)
			{
				// DONE Controller#onFilterChangedAction: add EdgeColorFilter to figuresFilter ...
				figuresFilter.add(new EdgeColorFilter(color));
			}
			else
			{
				// DONE Controller#onFilterChangedAction: remove any filter containing color from figuresFilter ...
				figuresFilter.removeFilterWith(color);
			}
		}

		if (item == filterLineTypeCheckMenuItem)
		{
			LineType lineType = drawingModel.getLineType();
			if (selected)
			{
				// DONE Controller#onFilterChangedAction: add LineTypeFilter(lineType) to figuresFilter ...
				figuresFilter.add(new LineTypeFilter(lineType));
			}
			else
			{
				// DONE Controller#onFilterChangedAction: removes any filter containing lineType from figuresFilter ...
				figuresFilter.removeFilterWith(lineType);
			}
		}

		if (item == filterLineWidthCheckMenuItem)
		{
			Double lineWidth = drawingModel.getLineWidth();
			if (selected)
			{
				// DONE Controller#onFilterChangedAction: add LineWidthFilter(lineWidth) to figuresFilter ...
				figuresFilter.add(new LineWidthFilter(lineWidth));
			}
			else
			{
				// DONE Controller#onFilterChangedAction: removes any filter containing lineWidth from figuresFilter ...
				figuresFilter.removeFilterWith(lineWidth);
			}
		}
		if (filteringProperty.get())
		{
			logger.info("filters = " + figuresFilter);
			FilteredList<Figure> filteredList = drawingModel.filtered(figuresFilter);
			figuresListView.setItems(filteredList);
			logger.info("filtered figures = " + filteredList);
		}
	}

	/**
	 * Clear all filters (and associated {@link CheckMenuItem}s)
	 * @param event the event to process
	 */
	@FXML
	public void onClearFiltersAction(ActionEvent event)
	{
		logger.info("Clear filters action triggered");

		/*
		 * Clears #figureTypesFilter #figuresFilter and re-add empty
		 * #figureTypesFilter
		 */
		figureTypesFilter.clear();
		figuresFilter.clear();
		figuresFilter.add(figureTypesFilter);

		/*
		 * Set all filterXXXCheckMenuItem to deselected
		 */
		filterCirclesCheckMenuItem.setSelected(false);
		filterEllipsesCheckMenuItem.setSelected(false);
		filterRectanglesCheckMenuItem.setSelected(false);
		filterRoundedRectanglesCheckMenuItem.setSelected(false);
		filterPolygonsCheckMenuItem.setSelected(false);
		filterNGonsCheckMenuItem.setSelected(false);
		filterStarsCheckMenuItem.setSelected(false);
		filterFillColorCheckMenuItem.setSelected(false);
		filterEdgeColorCheckMenuItem.setSelected(false);
		filterLineTypeCheckMenuItem.setSelected(false);
		filterLineWidthCheckMenuItem.setSelected(false);

		/*
		 * Re-set items on #figuresListView if required
		 */
		if (filteringProperty.get())
		{
			figuresListView.setItems(drawingModel.filtered(figuresFilter));
		}
	}

	/**
	 * Action to quit the application
	 * @param event event associated with this action
	 */
	@FXML
	public void onQuitAction(ActionEvent event)
	{
		quitActionImpl(event);
	}

	/**
	 * Implementation of the quit logic.
	 * Sends Vocabulary.byeCmd, sets {@link #commonRun} to false and close the
	 * stage.
	 * @param event the event passed to this callback (either {@link ActionEvent}
	 * or {@link WindowEvent} depending on what triggered this action).
	 */
	protected void quitActionImpl(Event event)
	{
		/*
		 * 	- closes the stage by
		 * 		- getting the stage from source if event is a WindowEvent
		 * 		- getting the stage from #parentStage or otherwise if event is
		 * 		an ActionEvent
		 */
		logger.info("Quit action triggered");

		Object source = event.getSource();
		Stage stage = null;

		if (event instanceof WindowEvent)
		{
			// Stage is the source
			stage = (Stage) source;
		}
		else if (event instanceof ActionEvent)
		{
			if (parentStage != null)
			{
				// We already have a registered stage
				stage = parentStage;
			}
			else
			{
				// Search for the stage
				if (source instanceof Button)
				{
					Button sourceButton = (Button) source;
					stage = (Stage) sourceButton.getScene().getWindow();
				}
				else
				{
					logger.warning("Unable to get Stage to close from: "
					    + source.getClass().getSimpleName());
				}
			}
		}
		else
		{
			logger.warning("Unknwon event source: " + event.getSource());
		}

		if (stage != null)
		{
			stage.close();
		}
		else
		{
			logger.warning("Window not closed");
		}
	}

	/**
	 * Action to show buttons with Graphics only
	 * @param event event associated with this action
	 */
	@FXML
	public void onDisplayButtonsWithGraphicsOnlyAction(ActionEvent event)
	{
		logger.info("Display Buttons with Graphics only action triggered");
		/*
		 * DONE Controller#onDisplayButtonsWithGraphicsOnlyAction ...
		 * setting all elts in #styleableButtons content display to graphics only
		 */
		for(Labeled l: styleableButtons){
			l.contentDisplayProperty().set(ContentDisplay.GRAPHIC_ONLY);
		}
	}

	/**
	 * Action to show buttons with Text and Graphics
	 * @param event event associated with this action
	 */
	@FXML
	public void onDisplayButtonsWithTextAndGraphicsAction(ActionEvent event)
	{
		logger.info("Display Buttons with Text and Graphics action triggered");
		/*
		 * DONE Controller#onDisplayButtonsWithTextAndGraphicsAction ...
		 * setting all elts in #styleableButtons content display to text and graphics
		 */
		for(Labeled l: styleableButtons){
			l.contentDisplayProperty().set(ContentDisplay.RIGHT);
		}
	}

	/**
	 * Action to show buttons with Text only
	 * @param event event associated with this action
	 */
	@FXML
	public void onDisplayButtonsWithTextOnlyAction(ActionEvent event)
	{
		logger.info("Display Buttons with Text only action triggered");
		/*
		 * DONE Controller#onDisplayButtonsWithTextOnlyAction ...
		 * setting all elts in #styleableButtons content display to text only
		 */
		for(Labeled l: styleableButtons){
			l.contentDisplayProperty().set(ContentDisplay.TEXT_ONLY);
		}
	}

	/**
	 * Action to set {@link #logger} level to {@link Level#INFO}
	 * @param event event associated with this action
	 */
	@FXML
	public void onSetLoggerLevelUpToInfoAction(ActionEvent event)
	{
		logger.info("Set Logger level up to INFO");
		// DONE Controller#onSetLoggerLevelUpToInfoAction ...
		logger.setLevel(Level.INFO);
	}

	/**
	 * Action to set {@link #logger} level to {@link Level#WARNING}
	 * @param event event associated with this action
	 */
	@FXML
	public void onSetLoggerLevelUpToWarningAction(ActionEvent event)
	{
		logger.info("Set Logger level up to WARNING");
		// DONE Controller#onSetLoggerLevelUpToWarningAction ...
		logger.setLevel(Level.WARNING);
	}

	/**
	 * Action to set {@link #logger} level to {@link Level#SEVERE}
	 * @param event event associated with this action
	 */
	@FXML
	public void onSetLoggerLevelUpToSevereAction(ActionEvent event)
	{
		logger.info("Set Logger level up to SEVERE");
		// DONE Controller#onSetLoggerLevelUpToSevereAction ...
		logger.setLevel(Level.SEVERE);
	}

	/**
	 * Action to set {@link #logger} level to {@link Level#OFF}
	 * @param event event associated with this action
	 */
	@FXML
	public void onSetLoggerLevelOffAction(ActionEvent event)
	{
		logger.info("Set Logger level to OFF");
		// DONE Controller#onSetLoggerLevelOffAction ...
		logger.setLevel(Level.OFF);
	}

	/**
	 * Action to set the number of Undo / Redos in {@link #historyManager}
	 * @param event event associated with this action
	 */
	@FXML
	public void onSetHistorySizeAction(ActionEvent event)
	{
		logger.info("Set History Size Action triggered");
		// DONE Controller#onSetHistorySizeAction ...
		Dialog<Integer> sizeDialog = new SizeDialogPane<Integer>(historyManager.size(),1,100,1,"Number of Undos/Redos");
		Optional<Integer> result = sizeDialog.showAndWait();
		historyManager.setSize(result.orElse(historyManager.size()));
	}

	/**
	 * Action to check if there is at least one {@link Color}, either Fill color
	 * or Edge Color selected to be applied on next Figure. And also reflects
	 * {@link #useFillColor} and {@link #useEdgeColor} on
	 * {@link #fillColorPicker} and {@link #edgeColorPicker} disabled property
	 * @param event event associated with this action
	 */
	@FXML
	public void onCheckColorsConsistencyAction(ActionEvent event)
	{
		logger.info("Check Colors consistency action triggered");
		Object source = event.getSource();
		if (!(source instanceof CheckBox))
		{
			logger.warning("source " + source.toString() + " is not a CheckBox");
			return;
		}

		CheckBox checkBox = (CheckBox) source;
		boolean selected = checkBox.isSelected();

		if (checkBox  == useFillColor)
		{
			// DONE Controller#onCheckColorsConsistencyAction: fill color case
			if (!selected && !useEdgeColor.isSelected()) {
				useEdgeColor.setSelected(true);
				edgeColorPicker.setDisable(false);
			}
			fillColorPicker.setDisable(!selected);
			return;
		}

		if (checkBox == useEdgeColor)
		{
			// DONE Controller#onCheckColorsConsistencyAction: edge color case
			if (!selected && !useFillColor.isSelected()) {
				useFillColor.setSelected(true);
				fillColorPicker.setDisable(false);
			}
			edgeColorPicker.setDisable(!selected);
			return;
		}

		logger.warning("event source is neither useFillColor nor useEdgeColor: "
		    + checkBox.toString());
	}

	/**
	 * Action called whenever the {@link #shapeTypeComboBox} changes value
	 * to ensure {@link #currentTool} is set to the right creation tool (iff
	 * edit mode is off, based on {@link #editToggleButton} selected status).
	 * @param event the event to process
	 */
	@FXML
	public void onShapeChangedAction(ActionEvent event)
	{
		Object source = event.getSource();
		if (!(source instanceof ComboBox<?>))
		{
			logger.warning("Shape type changed from unknonwn source : " + source);
		}

		/*
		 * TODO Controller#onShapeChangedAction ...
		 * 	- setTools according to the new type of figure
		 * 	- and #editToggleButton state
		 */
		//setTools(editToggleButton.isSelected());
		
	}

	/**
	 * Action called from {@link #figuresListView}'s context menu to clear
	 * current list selection
	 * @param event the event to process
	 */
	@FXML
	public void onClearSelectionAction(ActionEvent event)
	{
		logger.info("Clear selection action triggred");
		// DONE Controller#onClearSelectionAction ...
		drawingModel.clearSelection();
	}

	/**
	 * Binds two properties bi-directionnaly so when one property changes the
	 * other is also changed and vice-versa.
	 * e.g. #filterToggleButton and #filterToggleCheckMenuItem
	 * @param <T> The type of property
	 * @param prop1 the first proprety
	 * @param prop2 the second property
	 */
	private static <T> void bindProperties(Property<T> prop1, Property<T> prop2)
	{
		prop1.bindBidirectional(prop2);
	}

	/**
	 * Apply On/Off Icon on provided {@link ImageView} depending on the state
	 * of a {@link Toggle}able node (typically a {@link ToggleButton})
	 * @param toggle the toggle (button) containing the image view
	 * @param view the image view to change with new icon
	 * @param onIcon the image to apply when toggle is selected
	 * @param offIcon the image to apply when toggle is deselected
	 */
	private static void applyOnOffIcons(Toggle toggle,
	                                    ImageView view,
	                                    Image onIcon,
	                                    Image offIcon)
	{
		Image icon = toggle.isSelected() ? onIcon : offIcon;
		view.setImage(icon);
	}
	/**
	 * Apply On/Off Icon on provided {@link ImageView} depending on the state
	 * of a {@link javafx.scene.control.CheckMenuItem}
	 * @param checkMenuItem the check menu item  containing the image view
	 * @param view the image view to change with new icon
	 * @param onIcon the image to apply when toggle is selected
	 * @param offIcon the image to apply when toggle is deselected
	 */
	private void applyOnOffIcons(CheckMenuItem checkMenuItem,
	                             ImageView view,
	                             Image onIcon,
	                             Image offIcon)
	{
		Image icon = checkMenuItem.isSelected() ? onIcon : offIcon;
		view.setImage(icon);
	}
}
