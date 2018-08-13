import javax.swing.JFrame;

public class startUp{
	private static JFrame Frame;
	private static AbstractFactoryGui guiCreation=new AbstractFactoryGui();
	
	private static BudgetFacade budget=new BudgetFacade();
	private static RecFacade rec=new RecFacade();
	private static ChartsFacade charts=new ChartsFacade();
	
	private static ProjectFrames FrameInstance;
	public static void main(String[] args) {

		FrameInstance=guiCreation.orderFrame("Budget", budget);
		Frame=FrameInstance.getFrame();
		showFrame(Frame);
	}
public static void createBudgetFrame(){
	
	BudgetFactory factory = new BudgetFactory(budget);
	FrameInstance.createFrame(factory,Frame);
	
}
public static void createRecFrame(){
	ReconciliationFactory factory = new ReconciliationFactory(rec);
	FrameInstance.createFrame(factory,Frame);
}
public static void createChartFrame(){
	ChartsFactory factory = new ChartsFactory(charts);
	FrameInstance.createFrame(factory,Frame);
}
private static void showFrame(JFrame frame){
	frame.setSize(1000,700);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
}
}
