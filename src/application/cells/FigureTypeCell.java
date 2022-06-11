package application.cells;

import figures.enums.FigureType;

/**
 * Custom cell to display {@link FigureType}s in a {@link javafx.scene.control.ListView}.
 * Example:
 * {@code myListView.setCellFactory(listView -> new FigureTypeCell());}
 * @author davidroussel
 */
public class FigureTypeCell extends CustomCell<FigureType>
{
	/**
	 * Default constructor
	 * Loads FXML file FigureTypeCell.fxml to layout the cell and binds controller
	 * @implSpec The controller specified in FigureTypeCell.fxml is a {@link FigureTypeCellController}
	 */
	public FigureTypeCell()
	{
		super("FigureTypeCell.fxml");
	}
}
