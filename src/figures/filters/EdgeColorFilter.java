package figures.filters;

import figures.Figure;
import javafx.scene.paint.Color;

/**
 * Filter filtering figures based on their {@link EdgeColor}
 * @author davidroussel
 */
public class EdgeColorFilter extends FigureFilter<Color>
{
	/**
	 * Valued constructor
	 * @param element the type of figure to filter with
	 */
	public EdgeColorFilter(Color color)
	{
		super(color);
	}

	/**
	 * Predicate assessment
	 * @param f the figure to test
	 * @return true if the provided figure is of the same edge color as
	 * {@link EdgeColorFilter#element}
	 * @see java.util.function.Predicate#test(java.lang.Object)
	 */
	@Override
	public boolean test(Figure f)
	{
		return f.getEdgeColor() == element;
	}
}
