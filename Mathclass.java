
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.eclipsesource.json.*;
import com.eclipsesource.json.JsonObject.Member;
public class Mathclass {
	//JSON URL just latest https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/latest/owid-covid-latest.json
	//Country Codes https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes
	String format(Double a) {
		return String.valueOf(String.format("%.4f",a));
	}
	void getcasesfile(){
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL("https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/latest/owid-covid-latest.json").openStream());
				FileOutputStream fileOS = new FileOutputStream("cases.json")) {
			byte data[] = new byte[1024];
			int byteContent;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				fileOS.write(data, 0, byteContent);
			}
			fileOS.close();
			inputStream.close();
		} catch (IOException e) {
			// handles IO exceptions
		}
	}
	void filereadexample(){
		try { 
			File file = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader reader = new FileReader(file);
			JsonObject object = Json.parse(reader).asObject();
			//			String a = object.get("AFG").asObject().get("location").asString();

			for (Member member : object.get("AFG").asObject()) {
				System.out.println(member.getValue());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	void filewriteexample(){
		Writer writer;
		try {
			writer = new FileWriter("writeexample.json");
			JsonObject user = Json.object().add("name", "Alice").add("points", 23);
			user.writeTo(writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	String createuser(String name) {
		String ret = "";
		try { 
			File file = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			FileReader reader = new FileReader(file);
			JsonObject object = Json.parse(reader).asObject();
			Writer writer = new FileWriter("users.json");
			if (object.get(name)==null) {
				JsonObject userval = Json.object().set("money", 10000);
				ret+=("User "+name+" created with 10000€ starter money.\n");
				object.add(name, userval);
			}else {
				ret+=("Name is already present. No user was created.\n");
			}
			object.writeTo(writer);
			reader.close();
			writer.flush();
			writer.close();
		} catch (Exception e) {
			ret+=("Error");
			e.printStackTrace();
		}
		return ret;

	}
	String buy(String username,String country,String value) {
		String ret = "";
		try { 
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader userreader = new FileReader(userfile);
			FileReader casesreader = new FileReader(casesfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			JsonObject casesobj = Json.parse(casesreader).asObject();
			//Availability Checks
			double currentmoney=usersobj.get(username).asObject().getDouble("money", 0);
			if (currentmoney<Double.parseDouble(value) || Double.parseDouble(value)<0) {
				ret+=("Not enough Money or invalid Value. You only have "+format(usersobj.get(username).asObject().getDouble("money", 0))+"€.\n");
				return ret;
			}
			if (casesobj.get(country)==null) {
				ret+=("Error accessing case numbers from countrycode "+country+".\n");
				return ret;
			}
			if(usersobj.get(username)==null ) {
				ret+=("User not known. Create a User first.");
				return ret;
			}
			//Make Changes	
			double addshare=Double.parseDouble(value)/casesobj.get(country).asObject().get("new_cases").asDouble();
			usersobj.get(username).asObject().set("money", currentmoney-Double.parseDouble(value));
			if (usersobj.get(username).asObject().get(country)==null) {
				JsonObject sharejval = Json.object().add("percentage", addshare);
				usersobj.get(username).asObject().add(country, sharejval);							
			}else {
				double newshare=usersobj.get(username).asObject().get(country).asObject().getDouble("percentage", 0)+addshare;
				usersobj.get(username).asObject().get(country).asObject().set("percentage", newshare);	
			}
			double ownshare=usersobj.get(username).asObject().get(country).asObject().getDouble("percentage", 0);
			ret+=("Purchase of "+format(addshare*100)+"% Successful! "+username+" now owns "+format(ownshare*100)+"% of "+country+" cases. "+value+"€ was subtracted from your balance.\n");
			Writer writer = new FileWriter("users.json");
			Writer writer2 = new FileWriter("cases.json");
			usersobj.writeTo(writer);
			casesobj.writeTo(writer2);
			userreader.close();
			casesreader.close();
			writer.flush();
			writer.close();
			writer2.flush();
			writer2.close();

		} catch (Exception e) {
			ret+=("Error!(Hopefully) No changes were made.\n");
			e.printStackTrace();
		}
		return ret;
	}
	String sell(String username,String country,String value) {
		String ret = "";
		try {
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader userreader = new FileReader(userfile);
			FileReader casesreader = new FileReader(casesfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			JsonObject casesobj = Json.parse(casesreader).asObject();		
			//Availability Checks
			if (casesobj.get(country)==null) {
				ret+=("Error accessing case numbers from countrycode "+country+".\n");
				return ret;
			}
			if(usersobj.get(username)==null || usersobj.get(username).asObject().get(country)==null || Double.parseDouble(value)<0) {
				ret+=("User not known or does not own a stake in this country. Create a user or buy first to sell later\n");
				return ret;
			}
			double currentshare=usersobj.get(username).asObject().get(country).asObject().getDouble("percentage", 0);
			double currentcases = casesobj.get(country).asObject().get("new_cases").asDouble();
			if((currentshare*currentcases)<Double.parseDouble(value)) {
				ret+=("You dont have a big enough stake to sell "+value+"€ you only have "+format(currentshare*currentcases)+"€ worth of assets in this country.");
				return ret;
			}
			//Make Changes
			if (currentshare*currentcases==Double.parseDouble(value)) {
				usersobj.get(username).asObject().remove(country);	
			}else {
				usersobj.get(username).asObject().get(country).asObject().set("percentage", currentshare-(Double.parseDouble(value)/currentcases));
			}
			usersobj.get(username).asObject().set("money", usersobj.get(username).asObject().getDouble("money", 0)+Double.parseDouble(value));
			ret+=("Sale of "+format((Double.parseDouble(value)/currentcases)*100)+"% of "+country+" cases Successful! "+value+"€ was added to your balance");
			Writer writer = new FileWriter("users.json");
			Writer writer2 = new FileWriter("cases.json");
			usersobj.writeTo(writer);
			casesobj.writeTo(writer2);
			userreader.close();
			casesreader.close();
			writer.flush();
			writer.close();
			writer2.flush();
			writer2.close();
		} catch (Exception e) {
			ret+=("Error!(Hopefully) No changes were made.\n");
			e.printStackTrace();
		}
		return ret;
	}
	String sellall(String username,String country) {
		String ret = "";
		try {
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader userreader = new FileReader(userfile);
			FileReader casesreader = new FileReader(casesfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			JsonObject casesobj = Json.parse(casesreader).asObject();		
			//Availability Checks
			if (casesobj.get(country)==null) {
				ret+=("Error accessing case numbers from countrycode "+country+".\n");
				return ret;
			}
			if(usersobj.get(username)==null || usersobj.get(username).asObject().get(country)==null) {
				ret+=("User not known or does not own a stake in this country. Create a user or buy first to sell later\n");
				return ret;
			}
			double currentshare=usersobj.get(username).asObject().get(country).asObject().getDouble("percentage", 0);
			double currentcases = casesobj.get(country).asObject().get("new_cases").asDouble();
			//Make Changes
			usersobj.get(username).asObject().remove(country);	

			usersobj.get(username).asObject().set("money", usersobj.get(username).asObject().getDouble("money", 0)+currentshare*currentcases);
			ret+=("Sale of all "+country+" cases Successful! "+format(currentshare*currentcases)+"€ was added to your balance");
			Writer writer = new FileWriter("users.json");
			Writer writer2 = new FileWriter("cases.json");
			usersobj.writeTo(writer);
			casesobj.writeTo(writer2);
			userreader.close();
			casesreader.close();
			writer.flush();
			writer.close();
			writer2.flush();
			writer2.close();
		} catch (Exception e) {
			ret+=("Error!(Hopefully) No changes were made.\n");
			e.printStackTrace();
		}
		return ret;
	}
	String printassets(String username) {
		String ret = "";
		try {
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader userreader = new FileReader(userfile);
			FileReader casesreader = new FileReader(casesfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			JsonObject casesobj = Json.parse(casesreader).asObject();
			//Availability Check
			if(usersobj.get(username)==null ) {
				ret+=("User not known. Create a User first.");
				return ret;
			}
			//Go
			ret+=("Assets for "+username+": \n");
			double networth=0;
			int counter=0;
			for (Member member : usersobj.get(username).asObject()) {

				if (counter>0){
					double countrycases=casesobj.get(member.getName()).asObject().getDouble("new_cases",0);
					double worth=member.getValue().asObject().getDouble("percentage", 0)*countrycases;
					networth+=worth;
					ret+=(member.getName()+" "+format(member.getValue().asObject().getDouble("percentage", 0)*100)+"% "+" current value: "+format(worth)+"€ at "+format(countrycases)+" cases.\n");
				}else {
					ret+=(member.getName()+" "+format(member.getValue().asDouble())+"\n");
					networth+=member.getValue().asDouble();
				}
				counter++;
			}
			ret+=("Net worth is: "+format(networth)+"€\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ret;
	}
	String printnetworth(String username) {
		String ret = "";
		try {
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader userreader = new FileReader(userfile);
			FileReader casesreader = new FileReader(casesfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			JsonObject casesobj = Json.parse(casesreader).asObject();
			//Availability Check
			if(usersobj.get(username)==null ) {
				ret+=("User not known. Create a User first.");
				return ret;
			}
			//Go
			ret+=("User: "+username+"\n");
			double networth=0;
			int counter=0;
			for (Member member : usersobj.get(username).asObject()) {
				if (counter>0){
					double countrycases=casesobj.get(member.getName()).asObject().getDouble("new_cases",0);
					double worth=member.getValue().asObject().getDouble("percentage", 0)*countrycases;
					networth+=worth;
				}else {
					networth+=member.getValue().asDouble();
				}
				counter++;
			}
			ret+=("Net worth: "+format(networth)+"€\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ret;
	}
	String leaderboard() {
		String ret = "";
		try {
			File userfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\users.json");
			FileReader userreader = new FileReader(userfile);
			JsonObject usersobj = Json.parse(userreader).asObject();
			for (Member member : usersobj) {
				ret+=printnetworth(member.getName().toString())+"\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	String countryinfo(String country) {
		String ret = "";
		try {

			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader casesreader = new FileReader(casesfile);
			JsonObject casesobj = Json.parse(casesreader).asObject();
			//Checks
			if (casesobj.get(country)==null) {
				ret+=("Error accessing case numbers from countrycode "+country+".\n");
				return ret;
			}
			//GO
			int a = 1;
			for (Member member : casesobj.asObject().get(country).asObject()) {
				if (a<9) {
					ret+=(member.getName()+" "+member.getValue()+"\n");
				}
				a++;	
			}
		} catch (Exception e) {
			ret+=("Error!");
			e.printStackTrace();
		}
		return ret;
	}
	String countryinfofull(String country) {
		String ret = "";
		try {

			File casesfile = new File("C:\\Users\\Caraceus\\Desktop\\EclipseJavaWorkspace\\CoronaTrading\\cases.json");
			FileReader casesreader = new FileReader(casesfile);
			JsonObject casesobj = Json.parse(casesreader).asObject();
			//Checks
			if (casesobj.get(country)==null) {
				ret+=("Error accessing case numbers from countrycode "+country+".\n");
				return ret;
			}
			//GO
			for (Member member : casesobj.asObject().get(country).asObject()) {
					ret+=(member.getName()+" "+member.getValue()+"\n");
			}
		} catch (Exception e) {
			ret+=("Error!");
			e.printStackTrace();
		}
		return ret;
	}
	String IOControl(String str) {
		getcasesfile();
		try {
			String erg[] = str.split(" ");
			if (erg[0].equals("/buy")) return buy(erg[1],erg[2],erg[3]);
			if (erg[0].equals("/sell")) return sell(erg[1],erg[2],erg[3]);
			if (erg[0].equals("/sellall")) return sellall(erg[1],erg[2]);
			if (erg[0].equals("/portfolio")) return printassets(erg[1]);
			if (erg[0].equals("/leaderboard")) return leaderboard();
			if (erg[0].equals("/createuser")) return createuser(erg[1]);
			if (erg[0].equals("/countryinfo")) return countryinfo(erg[1]); 
			if (erg[0].equals("/countryinfofull")) return countryinfofull(erg[1]); 
			if (erg[0].equals("/help")) {
				return ("Commands:\n"
						+ "/createuser <username>\n"
						+ "/portfolio <username>\n"
						+ "/leaderboard\n"
						+ "/buy <username> <countrycode> <amount>\n"
						+ "/sell <username> <countrycode> <amount>\n"
						+ "/sellall <username> <countrycode>\n"
						+ "/countryinfo <countrycode>\n"
						+ "/countryinfofull <countrycode>\n"
						+ "/help\n"
						+ "/whatisthis\n"
						+ "Country Codes are available here https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes\n"
						+ "The casenumber used is available under countryinfo as new_cases\n"
						+ "Version 1.3\n");
						
			}
			if (erg[0].equals("/whatisthis")) {
				return ("Buy and sell shares of daily corona cases in countries around the world. Whoever has the most money in the end wins!");
			}
		}
		catch(Exception e){
			return ("Something went wrong. Stack Trace:+ \n"+e.getStackTrace().toString()+"\n");
		}
		return "Unknown Command "+str+". For a list of commands type 'help'.\n";

	}
}



