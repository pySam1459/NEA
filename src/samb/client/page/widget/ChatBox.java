package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.page.GamePage;
import samb.client.page.widget.listeners.TextBoxListener;
import samb.client.utils.Consts;
import samb.com.server.info.Message;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Func;
import samb.com.utils.enums.TableUseCase;

public class ChatBox extends Widget implements TextBoxListener {
	/* This subclass implements the chat functionality on the gamePage
	 * The class creates the widgets and images displayed on screen, 
	 *   and handles events relevant to the chat (Sending, Receiving, Rendering, etc)
	 * */
	
	private final Color BACKGROUND_COLOR = new Color(64, 81, 77, 127);
	private final Color BORDER_COLOR = new Color(58, 75, 72, 200);
	private final int BUFFER = 8;
	
	private final int chatSize = 14;
	private final Font chatFont = new Font("comicsansms", Font.PLAIN, chatSize), 
			chatFontBold = new Font("comicsansms", Font.BOLD, chatSize),
			chatFontItalics = new Font("comicsansms", Font.ITALIC, chatSize);
	private final Font chatNameFont = new Font("comicsansms", Font.BOLD, chatSize);
	private final Color chatColour = Consts.PAL1;
	
	private TextBox et;
	private BufferedImage chatImg;
	private List<Message> messages;

	public ChatBox(int[] rect, GamePage gp) {
		super(rect);
		
		initChatWidgets(gp);

		this.messages = new ArrayList<>();
		renderChat();
		
	}
	
	private void initChatWidgets(GamePage gp) {
		// This method creates the TextBox widget which a player enters their chat into
		
		this.et = new TextBox(new int[] {rect[0]+BUFFER, rect[1]+rect[3]-BUFFER*7,
				rect[2]-BUFFER*2, BUFFER*6}, "Chat Here...");
		et.round = false;
		et.underline = true;
		et.charLimit = 100;
		et.BACKGROUND_COLOUR = new Color(76, 93, 86, 0);
		et.addListener(this);
		et.HIDDEN = true;
		gp.add("enterText", et); // added to page
	}
	
	@Override
	public void onEnter(TextBox tb) {
		// This method is called by a TextBox Widget when the enter key is pressed, 
		//   ie when the player wants to send the message
		
		if(tb.id.equals("enterText")) {
			Message m = new Message(tb.getText(), Client.getClient().udata.userInfo.username);
			sendMessage(m); // sends the message to host, then to opposition
			addMessage(m);  // adds the message to the chat "log" 
			
			tb.setText("");
		}
	}
	
	public void setUseCase(TableUseCase tuc) {
		// This method tells the class what to render, 
		//   depended on whether the user is: playing, practicing or spectating
		String msg;
		et.HIDDEN = true;
		switch(tuc) {
		case playing:
			msg = "You can chat here...";
			et.HIDDEN = false;
			break;
		case practicing:
			msg = "You are on the practice table.";
			break;
		case spectating:
			msg = "You are spectating, not chatting.";
			break;
		default:
			msg = "Hopefully this message isn't seen, if it does something has gone wrong!";
		}
		addMessage(new Message(msg, "$PLAIN$"));
	}
	
	private void sendMessage(Message m) {
		// Sends the message typed to the host server to be forwarded to the opposition
		Packet p = new Packet(Header.chat);
		p.message = m;
		
		Client.getClient().server.send(p);
		
	}
	
	public void addMessage(Message msg) {
		// When a new message is received, the chat is rendered again with the new message
		messages.add(msg);
		renderChat();
		
	}
	

	@Override
	public void tick() {
		super.animTick();

	}

	@Override
	public void render(Graphics2D graph) {
		// This method renders the chat box and the chat inside that box
		// The 'chatImg' is created by the renderChat method, not in this method every tick
		
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		// Box
		g.setColor(BACKGROUND_COLOR);
		g.fillRoundRect(BUFFER/2, BUFFER/2, rect[2]-BUFFER, rect[3]-BUFFER, BUFFER, BUFFER);
		
		g.setStroke(new BasicStroke(2));
		g.setColor(BORDER_COLOR);
		g.drawRoundRect(BUFFER/2, BUFFER/2, rect[2]-BUFFER, rect[3]-BUFFER, BUFFER, BUFFER);
		
		// Chat
		g.drawImage(chatImg, 0, 0, null);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}
	
	private void renderChat() {
		// This method creates a BufferedImage of the chat, so that it fits in the chatBox and auto-scrolls
		// When a player sends a chat, if the last chat wasn't them, it will display their name in bold, then the message
		// Otherwise, it will display their chat below their previous chat
		
		int biggerBuffer = et.HIDDEN ? BUFFER : BUFFER*9;
		chatImg = new BufferedImage(rect[2]-BUFFER, rect[3]-biggerBuffer, BufferedImage.TYPE_INT_ARGB);
		
		int h = (int)((messages.size()+1)*3*chatSize*1.2); // All of the chat
		BufferedImage rawChat = new BufferedImage(rect[2]-BUFFER, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) rawChat.getGraphics();
		g.setColor(chatColour);
		
		int y = 0;
		String fromLast = "", text="", test="";
		String[] parts;
		
		g.setFont(chatFont);
		FontMetrics fm = g.getFontMetrics();
		int w;
		
		for(Message m: messages) {
			text="";
			test="";
			
			// if last player is different to new chat or 'm.from' is a specific program flag
			if(!fromLast.equals(m.from) && !Func.isFlag(m.from)) { 
				g.setFont(chatNameFont);
				fromLast = m.from;
				
				y += chatSize*1.2;
				g.drawString(m.from, (int)(BUFFER*1.5), y+chatSize);
				y += chatSize*1.2;
				
			} if(Func.isFlag(m.from) && !"$BOLD NOSPACE$".equals(m.from)) {
				y += chatSize;
			}
			
			// render message, each word is separated to check if the sentence 
			//   will be longer than the chat box, if so wrap around
			g.setFont(chatFont);
			parts = m.text.split(" ");
			
			if(Func.isFlag(m.from)) {
				renderDoFlag(g, m.from);
			}
			
			for(String s: parts) {
				test += s + " ";
				w = (int) fm.getStringBounds(test, g).getWidth();
				if(w > rect[2]-BUFFER*2) {
					g.drawString(text, (int)(BUFFER*1.5), y+chatSize);
					y += chatSize*1.2;
					text = s + " ";
					test = s + " ";
				} else {
					text = test;
				}
			}
			
			g.drawString(text, (int)(BUFFER*1.5), y+chatSize);
			y += chatSize*1.2;

		}

		// The rawChat BufferedImage is the whole chat, needs to be cropped into size
		BufferedImage cropChat;
		y += chatSize/2;
		if(y-chatImg.getHeight() > 0) {
			cropChat = rawChat.getSubimage(0, y-chatImg.getHeight()+BUFFER, chatImg.getWidth()-BUFFER, chatImg.getHeight());
		} else {
			cropChat = rawChat.getSubimage(0, 0, chatImg.getWidth()-BUFFER, y);
		}
		
		g = (Graphics2D) chatImg.getGraphics();
		g.drawImage(cropChat, 0, BUFFER, null);
		
	}
	
	private void renderDoFlag(Graphics2D g, String flag) {
		// If the 'from' is a flag, do that flag
		switch(flag) {
		case "$PLAIN$":
			g.setFont(chatFont);
			break;
		case "$BOLD$":
		case "$BOLD NOSPACE$":
			g.setFont(chatFontBold);
			break;
		case "$ITALICS":
			g.setFont(chatFontItalics);
			break;
		}
	}

}
