package figures.filters;

import figures.Figure;
import javafx.scene.paint.Color;

/**
 * Filter filtering figures based on their {@link FillColor}
 * @author davidroussel
 */
public class FillColorFilter extends FigureFilter<Color>
{
	/**
	 * Valued constructor
	 * @param element the type of figure to filter with
	 */
	public FillColorFilter(Color color)
	{
		super(color);
	}

	/**
	 * Predicate assessment
	 * @param f the figure to test
	 * @return true if the provided figure is of the same fill color as
	 * {@link EdgeColorFilter#element}
	 * @see java.util.function.Predicate#test(java.lang.Object)
	 */
	@Override
	public boolean test(Figure f)
	{
		return f.getFillColor() == element;
	}
}
