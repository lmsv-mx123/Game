package com.openrsc.server.plugins.npcs;

import com.openrsc.server.Constants;
import com.openrsc.server.external.CerterDef;
import com.openrsc.server.external.EntityHandler;
import com.openrsc.server.model.container.Item;
import com.openrsc.server.model.entity.npc.Npc;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.plugins.listeners.action.TalkToNpcListener;
import com.openrsc.server.plugins.listeners.executive.TalkToNpcExecutiveListener;
import com.openrsc.server.util.rsc.DataConversions;

import static com.openrsc.server.plugins.Functions.npcTalk;
import static com.openrsc.server.plugins.Functions.playerTalk;
import static com.openrsc.server.plugins.Functions.showMenu;

public class Certer implements TalkToNpcListener, TalkToNpcExecutiveListener {

	int[] certers = new int[]{225, 226, 227, 466, 467, 299, 341, 369,
		370, 348, 267};

	@Override
	public void onTalkToNpc(Player p, final Npc n) {

		// Forester (Log certer; custom)
		if ((n.getID() == 348)
			&& !Constants.GameServer.WANT_WOODCUTTING_GUILD) {
			return;
		}

		final CerterDef certerDef = EntityHandler.getCerterDef(n.getID());
		if (certerDef == null) {
			return;
		}
		
		beginCertExchange(certerDef, p, n);
	}
	
	private void beginCertExchange(CerterDef certerDef, Player p, Npc n) {
		npcTalk(p, n, "Welcome to my " + certerDef.getType()
			+ " exchange stall");
		
		String ending = (n.getID() == 226 || n.getID() == 341 || n.getID() == 467 ? "s" : "");
		
		// First Certer Menu
		int firstType = firstMenu(certerDef, ending, p, n);
		if (firstType < 0) return;
		
		if (p.isBusy()) {
			return;
		}
		
		int secondType = -1;
		
		//informational only
		if (firstType != 2) {
			// Second Certer Menu
			secondType = secondMenu(certerDef, ending, p, n, firstType);
			if (secondType < 0) return;
			
			if (p.isBusy()) {
				return;
			}
		}
		
		// Final Certer Menu
		switch (firstType) {
			case 0: //cert to item
				decertMenu(certerDef, ending, p, n, secondType);
				break;
			case 1: //item to cert
				certMenu(certerDef, ending, p, n, secondType);
				break;
			case 2: //informational
				infMenu(certerDef, ending, p, n);
				break;
		}
	}
	
	private int firstMenu(CerterDef certerDef, String ending, Player p, Npc n) {
		return showMenu(p, "I have some certificates to trade in",
				"I have some " + certerDef.getType() + ending + " to trade in",
				"What is a " + certerDef.getType() + " exchange stall?");	
	}
	
	private int secondMenu(CerterDef certerDef, String ending, Player p, Npc n, int option) {
		if (option == -1)
			return -1;
		
		final String[] names = certerDef.getCertNames();
		switch(option) {
			case 0:
				p.message("what sort of certificate do you wish to trade in?");
				return showMenu(p, names);
			case 1:
				p.message("what sort of " + certerDef.getType() + ending + " do you wish to trade in?");
				return showMenu(p, names);
			default:
				return -1;
		}
	}
	
	private void decertMenu(CerterDef certerDef, String ending, Player p, Npc n, int index) {
		final String[] names = certerDef.getCertNames();
		p.message("How many certificates do you wish to trade in?");
		int certAmount;
		if (Constants.GameServer.WANT_CERTER_BANK_EXCHANGE) {
			certAmount = showMenu(p, "One", "two", "Three", "four",
				"five", "All to bank");
		} else {
			certAmount = showMenu(p, "One", "two", "Three", "four", "five");
		}
		int certID = certerDef.getCertID(index);
		if (certID < 0) {
			return;
		}
		int itemID = certerDef.getItemID(index);
		if (certAmount == 5) {
			if (p.isIronMan(2)) {
				p.message("As an Ultimate Iron Man. you cannot use certer bank exchange.");
				return;
			}
			certAmount = p.getInventory().countId(certID);
			if (certAmount <= 0) {
				p.message("You don't have any " + names[index]
					+ " certificates");
				return;
			}
			Item bankItem = new Item(itemID, certAmount * 5);
			if (p.getInventory().remove(new Item(certID, certAmount)) > -1) {
				p.message("You exchange the certificates, "
					+ bankItem.getAmount() + " "
					+ bankItem.getDef().getName()
					+ " is added to your bank");
				p.getBank().add(bankItem);
			}
		} else {
			certAmount += 1;
			int itemAmount = certAmount * 5;
			if (p.getInventory().countId(certID) < certAmount) {
				p.message("You don't have that many certificates");
				return;
			}
			if (p.getInventory().remove(certID, certAmount) > -1) {
				p.message("You exchange your certificates for "
					+ certerDef.getType() + ending);
				for (int x = 0; x < itemAmount; x++) {
					p.getInventory().add(new Item(itemID, 1));
				}
			}
		}
	}
	
	private void certMenu(CerterDef certerDef, String ending, Player p, Npc n, int index) {
		final String[] names = certerDef.getCertNames();
		p.message("How many " + certerDef.getType() + ending
			+ " do you wish to trade in?");
		int certAmount;
		if (Constants.GameServer.WANT_CERTER_BANK_EXCHANGE) {
			certAmount = showMenu(p, "five", "ten", "Fifteen", "Twenty", "Twentyfive",
					"All from bank");
		} else {
			certAmount = showMenu(p, "five", "ten", "Fifteen", "Twenty", "Twentyfive");
		}
		int certID = certerDef.getCertID(index);
		if (certID < 0) {
			return;
		}
		int itemID = certerDef.getItemID(index);
		if (certAmount == 5) {
			if (p.isIronMan(2)) {
				p.message("As an Ultimate Iron Man. you cannot use certer bank exchange.");
				return;
			}
			certAmount = (int) (p.getBank().countId(itemID) / 5);
			int itemAmount = certAmount * 5;
			if (itemAmount <= 0) {
				p.message("You don't have any " + names[index] + " to cert");
				return;
			}
			if (p.getBank().remove(itemID, itemAmount) > -1) {
				p.message("You exchange the " + certerDef.getType() + ", "
					+ itemAmount + " "
					+ EntityHandler.getItemDef(itemID).getName()
					+ " is taken from your bank");
				p.getInventory().add(new Item(certID, certAmount));
			}
		} else {
			certAmount += 1;
			int itemAmount = certAmount * 5;
			if (p.getInventory().countId(itemID) < itemAmount) {
				p.message("You don't have that " + (ending.equals("") ? "much" : "many") 
						+ " " + certerDef.getType() + ending);
				return;
			}
			p.message("You exchange your " + certerDef.getType() + ending
				+ " for certificates");
			for (int x = 0; x < itemAmount; x++) {
				p.getInventory().remove(itemID, 1);
			}
			p.getInventory().add(new Item(certID, certAmount));
		}
	}
	
	private void infMenu(CerterDef certerDef, String ending, Player p, Npc n) {
		String item;
		switch(certerDef.getType()) {
			case "ore":
				item = "ores";
				break;
			case "bar":
				item = "bars";
				break;
			case "fish":
				item = "fish";
				break;
			case "log":
				item = "logs";
				break;
			default:
				item = certerDef.getType();
				break;
		}
		playerTalk(p, n, "What is a " + certerDef.getType() + " exchange store?");
		npcTalk(p, n, "You may exchange your " + item + " here",
				"For certificates which are light and easy to carry",
				"You can carry many of these certificates at once unlike " + item,
				"5 " + item + " will give you one certificate",
				"You may also redeem these certificates here for " + item + " again",
				"The advantage of doing this is",
				"You can trade large amounts of " + item + " with other players quickly and safely");
	}

	@Override
	public boolean blockTalkToNpc(Player p, Npc n) {
		return DataConversions.inArray(certers, n.getID());
	}
}
