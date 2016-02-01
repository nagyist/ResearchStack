package co.touchlab.researchstack.core.answerformat;

/**
 * Created by bradleymcdermott on 10/13/15.
 */
public class IntegerAnswerFormat extends AnswerFormat
{
    private int maxValue;
    private int minValue;

    // TODO Params should be reserved.
    public IntegerAnswerFormat(int maxValue, int minValue)
    {
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    @Override
    public QuestionType getQuestionType()
    {
        return Type.Integer;
    }

    public int getMaxValue()
    {
        return maxValue;
    }

    public int getMinValue()
    {
        return minValue;
    }
}
