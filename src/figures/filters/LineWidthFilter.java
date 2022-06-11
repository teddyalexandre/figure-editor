package figures.filters;

import figures.Figure;

/**
 * Filter filtering figures based on their {@link LineWidth}
 * @author davidroussel
 */
public class LineWidthFilter extends FigureFilter<Double>
{
	/**
	 * Valued constructor
	 * @param element the type of figure to filter with
	 */
	public LineWidthFilter(Double element)
	{
		super(element);
	}

	/**
	 * Predicate assessment
	 * @param f the figure to test
	 * @return true if the provided figure is of the same line width as
	 * {@link FigureFilter#element}
	 * @see java.util.function.Predicate#test(java.lang.Object)
	 */
	@Override
	public boolean test(Figure f)
	{
		return f.getLineWidth() == element;
	}
}
