package figures.filters;

import figures.Figure;
import figures.enums.LineType;

/**
 * Filter filtering figures based on their {@link LineType}
 * @author davidroussel
 */
public class LineTypeFilter extends FigureFilter<LineType>
{
	/**
	 * Valued constructor
	 * @param element the type of figure to filter with
	 */
	public LineTypeFilter(LineType element)
	{
		super(element);
	}

	/**
	 * Predicate assessment
	 * @param f the figure to test
	 * @return true if the provided figure has the same line type as
	 * {@link FigureFilter#element}
	 * @see java.util.function.Predicate#test(java.lang.Object)
	 */
	@Override
	public boolean test(Figure f)
	{
		return f.getLineType() == element;
	}
}
