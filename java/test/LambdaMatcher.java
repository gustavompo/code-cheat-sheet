class λ{
  public static <T> LambdaMatcher<T> matcher(Function<T, Boolean> matchFn){
    return new LambdaMatcher<>(matchFn);
  }
}

class LambdaMatcher<T> extends BaseMatcher<T>
{

  private final Function<T, Boolean> matchFn;

  public LambdaMatcher(Function<T, Boolean> matchFn) {
    this.matchFn = matchFn;
  }

  @Override
  public boolean matches(Object value) {
    T castValue;
    try{
      castValue = (T) value;
    }catch(Exception e){ return false;}

    return  matchFn.apply(castValue);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("does not match according to the given match expression");
  }
}
