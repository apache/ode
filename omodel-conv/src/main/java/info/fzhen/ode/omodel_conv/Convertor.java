package info.fzhen.ode.omodel_conv;

import spoon.Launcher;

/**
 * Main class to execute the convert.
 * @author fangzhen
 *
 */
public class Convertor{
	public static void main(String[] args) throws Exception {
		String paths[] = new String[]{
				"./spoon/orig-classes",
				"./spoon/spooned1",
				"./spoon/spooned2",
//				"./spoon/spooned3",
				"./spoon/spooned",
		};
		int pi = 0;
		
		String[] args1 = new String[] {
				"--fragments", 
				"--output-type", "compilationunits",
				"--with-imports",
				"-i", paths[pi++],
				"-o", paths[pi], "-p", 
				"info.fzhen.ode.omodel_conv.FieldAccessorGen"
				};
		Launcher launcher1 = new Launcher();
		launcher1.setArgs(args1);
		launcher1.run();

		String[] args2 =  new String[] {
				"--fragments", 
				"--output-type", "compilationunits",
				"--with-imports",
				"-i", paths[pi++],
				"-o", paths[pi], "-p", 
				"info.fzhen.ode.omodel_conv.FieldAssign2Setter"
				};
		Launcher launcher2 = new Launcher();
		launcher2.setArgs(args2);
		launcher2.run();

		String[] args3 =  new String[] {
				"--fragments", 
				"--output-type", "compilationunits",
				"--with-imports",
				"-i", paths[pi++],
				"-o", paths[pi], "-p", 
				"info.fzhen.ode.omodel_conv.FieldAcc2Getter"
				};
		Launcher launcher3 = new Launcher();
		launcher3.setArgs(args3);
		launcher3.run();
		
	}
}
