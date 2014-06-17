package info.fzhen.ode.omodel_conv;

import spoon.Launcher;

public class Convertor{
	public static void main(String[] args) throws Exception {
		//		CtSimpleType<?> type = TestUtils.build("info.fzhen.ode.omodel_conv", "Mouse");
		//		List<CtField<?>> fields = type.getFields();	

		Launcher launcher = new Launcher();
		launcher.setArgs(args);
		launcher.run();
	}

	


}
