package trading.util;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import trading.util.Event.EventResult;

import io.ReadForexCalendar;

/**
 * EventInterpreter downloads a html file from forexfactory.com and tries to get
 * a result froma particular economic event specified.
 * 
 * @author Tobias W
 * 
 */
public class EventInterpreter {

	private final ReadForexCalendar rfc;

	public EventInterpreter() {
		rfc = new ReadForexCalendar("interpret.html");
	}

	/**
	 * Checks with forexfactory.com for this event title and interprets its
	 * result. There are several possible outcomes. One is that the result is
	 * not better nor worse, which means a result isn't given. Another is that
	 * the event didn't even have a forecast, which makes the result useless in
	 * terms of predicament of currency outcome.
	 * 
	 * The third is that one event affects several currencies. IN THIS CASE this
	 * method is implemented so EUR and USD is prioritized. So if a event called
	 * "Trade Balance" both has a AUD and a USD tag, it will choose the USD tag
	 * and return its result.
	 * 
	 * @param eventTitle
	 * @return
	 */
	public Event interpret(Event e) {
		Event event = new Event(e.getTitle());
		event.setImpact(e.getImpact());
		event.setInstrument(e.getInstrument());

		Pattern pattern = Pattern
				.compile("<td class=\"time\">([0-9]?[0-9]:[0-9][0-9][a-z][a-z])?</td> <td class=\"currency\">[A-Z][A-Z][A-Z]</td> <td class=\"impact\"> <span title=\"([A-Z]*[a-z]* *)*\" class=\"[a-z]*\"></span> </td> <td class=\"event\"><span>"
						+ e.getTitle()
						+ "</span></td> <td class=\"detail\"><a class=\"calendar_detail level[0-9]\" data-level=\"[0-9]\"></a></td> <td class=\"actual\"> <span class=\"[a-z]*\">-?[0-9]*\\.*[0-9]*%?[A-Z]?</span>");
		rfc.downloadFile("http://www.forexfactory.com/");

		String fileContent = "";

		try {
			fileContent = new Scanner(rfc.getFile()).useDelimiter("\\Z").next();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		Matcher matcher = pattern.matcher(fileContent);

		boolean foundPrimeResult = false;
		String eventString = "";
		while (matcher.find()) {
			eventString = matcher.group();
			if (eventString.contains("EUR") || eventString.contains("USD"))
				break;
			foundPrimeResult = true;
		}

		// System.out.println(eventString);

		Pattern resultPattern = Pattern
				.compile("<td class=\"actual\"> <span class=\"[a-z]*\">-?[0-9]*\\.*[0-9]*%?[A-Z]?</span>");

		// match and set result
		matcher = resultPattern.matcher(eventString);
		if (matcher.find()) {
			pattern = Pattern.compile("<span class=\"[a-z]*\">");
			Matcher matcher2 = pattern.matcher(matcher.group());
			if (matcher2.find()) {
				String result = matcher2.group();
				if (result.contains("better"))
					event.setResult(EventResult.BETTER);
				else if (result.contains("worse"))
					event.setResult(EventResult.WORSE);
				else
					event.setResult(EventResult.NONE);

			}

		}

		// check if unchanged but result is in
		// TODO fixa denna s? att den inte matcher med events som ?nnu inte har
		// kommit in.
		// Fan sv?rt att matcha unchanged faktiskt tobbe
		if (!foundPrimeResult) {
			pattern = Pattern
					.compile(e.getTitle()
							+ "</span></td> <td class=\"detail\"><a class=\"calendar_detail level[0-9]\" data-level=\"[0-9]\"></a></td> <td class=\"actual\">*[0-9]+.*</td>");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				event.setResult(EventResult.UNCHANGED);

			}
		}

		return event;

	}
}
