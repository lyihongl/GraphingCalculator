package main;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Calculator extends Application {

	private Group mainGroup;

	private double scaleY, scaleX;

	private String postFix = "";

	public Calculator() {

	}

	/**
	 * Converts infix expression to postfix expression. Infix is how we normally
	 * write expressions, postfix is more easily parsed by the comptuer.
	 * 
	 * @param equation
	 *            The equation that should be converted to postFix
	 * @return Postfix expression in String form
	 */
	public static String inFixToPostFix(String equation) {
		String postFix = "";

		equation = equation.replaceAll("\\s+", "");

		Pattern pattern = Pattern.compile("\\d+\\p{Alpha}");
		Matcher match = pattern.matcher(equation);

		equation = equation.replaceAll("(?<=\\p{Alpha})(?=\\d)|(?<=\\d)(?=\\p{Alpha})", "*");
		equation = equation.replaceAll("^-|\\-(?=\\()", "-1*");
		equation = equation.replaceAll("\\)\\(",")*(");
		equation = equation.replaceAll("-x", "-1*x");
//		equation = equation.replaceAll("(?<=\\^)\\-", "(-");

		// context of "-"

		// finding 3*x or 3/x
		String vars = "(\\p{Alpha})";

		// letters without coefficients
		String coeff = "(\\d*\\.?\\d*\\.?\\d+|^\\-\\d+|(?<=\\()\\-\\d+)|(?<=\\p{Punct})\\-\\d+";
//		String coeffDec = "\\d+";
		
		
		// operators
		String oper = "\\+|(?<!^)\\-|\\*|\\/|\\^";
		String oper2 = "\\+|\\-|\\*|\\/|\\^";

		String brackets = "\\(|\\)";

		pattern = Pattern.compile(vars + "|" + coeff + "|" + oper + "|" + brackets);
		match = pattern.matcher(equation);

		LinkedList<String> stack = new LinkedList<String>();

		Hashtable<String, Integer> priority = new Hashtable<String, Integer>();

		priority.put("+", 0);
		priority.put("-", 0);
		priority.put("/", 1);
		priority.put("*", 1);
		priority.put("^", 2);

		priority.put("(", 2);
		priority.put(")", 2);
		boolean first = true;
		while (match.find()) {
//			System.out.println(match.group());
			if (match.group().matches(vars + "|" + coeff)) {

				postFix = postFix + match.group() + " ";

			} else if (match.group().matches(oper2) || match.group().matches("\\(")) {

				if (stack.isEmpty()) {
					stack.push(match.group());
					first = true;

				} else if (priority.get(match.group()).intValue() < priority.get(stack.peek()).intValue()) {

					while (!stack.isEmpty() && !stack.peek().matches(brackets)) {
						// //System.out.println(stack.peek());
						postFix = postFix + stack.peek() + " ";
						stack.pop();
						// //System.out.println(stack.peek());

					}
				}

				if (!first) {
					stack.push(match.group());

				}
				first = false;
			} else if (match.group().matches("\\)")) {
				while (!stack.peek().matches("\\(")) {
					// //System.out.println(stack.peek());
					postFix = postFix + stack.peek() + " ";
					stack.pop();
					// //System.out.println(stack.peek());
				}
				stack.pop();
			}

		}
		if (!stack.isEmpty()) {
			while (!stack.isEmpty()) {
				postFix = postFix + stack.peek() + " ";
				stack.pop();
			}
		}

		System.out.println(equation);
		//System.out.println(postFix);
		return postFix;
	}

	/**
	 * Evaluates a postfix expression at value x
	 * 
	 * @param x
	 * @return double
	 */
	public double evaluatePostfix(double x) {

		Scanner scan = new Scanner(postFix);
		String s;
		LinkedList<Double> stack = new LinkedList<Double>();
		double temp;
		double partSum = 0;

		while (scan.hasNext()) {
			s = scan.next();
			if (s.matches("\\p{Alpha}")) {
				s = x + "";
			}

			if (s.matches("(^-?\\d*\\.\\d+|^-?\\d+\\.\\d*$|^-?\\d+)|(\\d*\\.\\d+|-?\\d+\\.\\d*$|\\d+)")) {
				stack.push(Double.parseDouble(s));
			} else if (s.matches("\\+|\\-(?<!\\d)|\\*|\\/|\\^")) {
				temp = stack.peek();
				stack.pop();
				if (s.equals("+")) {
					partSum = stack.peek() + temp;
					stack.pop();
				} else if (s.equals("-")) {
					partSum = stack.peek() - temp;
					stack.pop();
				} else if (s.equals("*")) {
					partSum = stack.peek() * temp;
					stack.pop();
				} else if (s.equals("/")) {
					partSum = stack.peek() / temp;
					stack.pop();
				} else if (s.equals("^")) {
					partSum = Math.pow(stack.peek(), temp);
					stack.pop();
				}
				stack.push(partSum);
			}
		}

		return stack.peek();
	}

	public void graph(GraphicsContext gc) {

		scaleX = 40;
		scaleY = 40;
		for (int i = -300; i < 300; i++) {
			gc.strokeLine(i + 300, scaleY * evaluatePostfix(i / scaleX) * -1 + 300, i + 301,
					scaleY * evaluatePostfix((i + 1.0) / scaleX) * -1 + 300);
//			gc.strokeLine(x1, y1, x2, y2);
		}
		
	}

	private TextField field = new TextField();
	private String equation = "0";

	public void init(){

		Canvas root = new Canvas(600, 600);
		mainGroup = new Group(root);

		final GraphicsContext gc = root.getGraphicsContext2D();

		Button b = new Button("Enter");
		
		b.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent event){
				update(gc);
			}

		});

		HBox box = new HBox();

		box.setPadding(new Insets(15, 12, 15, 12));
		box.setSpacing(10);

		box.getChildren().add(field);
		box.getChildren().add(b);

		mainGroup.getChildren().add(box);

		try {
			postFix = inFixToPostFix(equation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		graph(gc);

	}

	public void update(GraphicsContext gc) {
		gc.clearRect(0, 0, 600, 600);
		equation = field.getText();
		try {
			postFix = inFixToPostFix(equation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		graph(gc);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Scene scene = new Scene(mainGroup);

		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch();
	}

}
