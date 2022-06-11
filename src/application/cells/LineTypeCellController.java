package application.cells;

import figures.Figure;
import figures.enums.FigureType;
import figures.enums.LineType;
import utils.IconFactory;

/**
 * Controller for cells representing {@link LineType}s
 * @author davidroussel
 */
public class LineTypeCellController extends AbstractCustomCellController<LineType>
{
	/**
	 * Set image in {@link #iconView} depending on the provided figure
	 * @param figure the figure to provide icon to
	 */
	@Override
	public void setIcon(LineType lineType)
	{
		switch(lineType){
			case SOLID:
				setImage(IconFactory.getIcon("Stroke_Solid"));
				break;
			case DASHED:
				setImage(IconFactory.getIcon("Stroke_Dashed"));
				break;
			case NONE:
				setImage(IconFactory.getIcon("Stroke_None"));
		}
	}
}
