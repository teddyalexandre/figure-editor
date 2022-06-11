package application.cells;

import figures.enums.FigureType;

/**
 * Controller for cells representing {@link FigureType}s
 * @author davidroussel
 */
public class FigureTypeCellController extends AbstractCustomCellController<FigureType>
{
	/**
	 * Set image in {@link #iconView} depending on the provided figure
	 * @param figure the figure to provide icon to
	 */
	@Override
	public void setIcon(FigureType figureType)
	{
		setImage(FigureIconsFactory.getIconFromType(figureType));
	}
}
