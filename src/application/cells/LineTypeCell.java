package application.cells;

import figures.enums.LineType;

/**
 * Custom cell to display {@link LineType}s in a {@link javafx.scene.control.ListView}.
 * Example:
 * {@code myListView.setCellFactory(listView -> new FigureCell());}
 * @author davidroussel
 */
public class LineTypeCell extends CustomCell<LineType>
{
	/**
	 * Default constructor
	 * Loads FXML file FigureCell.fxml to layout the cell and binds controller
	 * @implSpec The controller specified in FigureCell.fxml is a {@link LineTypeCellController}
	 */
	public LineTypeCell()
	{
		super("LineTypeCell.fxml");
	}
}
