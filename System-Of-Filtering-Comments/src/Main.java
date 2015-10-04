public class Main {

    public static void main(String[] args) {

        TextAnalyzer[] textAnalyzers = new TextAnalyzer[] {
            new NegativeTextAnalyzer()
          , new SpamAnalyzer(new String [] {"100% Free"})
          , new TooLongTextAnalyzer(20)
        };

        String[] texts = new String[] { "Hello World!", "100% Free Games", ":(", "100% Free Games Download ...", "Email Spam Words to Avoid" };
        for (String text : texts) {
            System.out.print(text + " -> ");
            Label label = new Main().checkLabels(textAnalyzers, text);
            switch (label) {
                case SPAM: System.out.println("SPAM");
                    break;
                case NEGATIVE_TEXT: System.out.println("NEGATIVE_TEXT");
                    break;
                case TOO_LONG: System.out.println("TOO_LONG");
                    break;
                case OK: System.out.println("OK");
                    break;
            }
        }

    }

    public Label checkLabels(TextAnalyzer[] analyzers, String text) {
        for (TextAnalyzer textAnalyzer : analyzers) {
            Label label = textAnalyzer.processText(text);
            if (label != Label.OK) {
                return label;
            }
        }
        return Label.OK;
    }
}
