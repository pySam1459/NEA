package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.game.GamePage;
import samb.client.main.Client;
import samb.com.server.info.Message;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class ChatBox extends Widget implements TextBoxListener {
	
	private final Color BACKGROUND_COLOR = new Color(64, 81, 77, 127);
	private final Color BORDER_COLOR = new Color(58, 75, 72, 200);
	private final int BUFFER = 8;
	
	private BufferedImage chatImg;
	private List<Message> messages;

	public ChatBox(int[] rect, GamePage gp) {
		super(rect);

		this.messages = new ArrayList<>();
		messages.add(new Message("Welcome to Pool, you can chat here...", null));
		renderChat();
		
		initChatWidgets(gp);
		
	}
	
	private void initChatWidgets(GamePage gp) {
		TextBox et = new TextBox(new int[] {rect[0]+BUFFER, rect[1]+rect[3]-BUFFER*8,
				rect[2]-BUFFER*2, BUFFER*8}, "Chat Here...");
		et.textRegex = ".";
		et.addListener(this);
		gp.add("enterText", et);
	}
	
	@Override
	public void onEnter(Widget w) {
		// This method is called by a TextBox Widget when the enter key is pressed
		
		if(w.id.equals("enterText")) {
			TextBox tb = (TextBox) w;
			if(tb.getText().matches(".+")) {
				Message m = new Message(tb.getText(), Client.getClient().udata.userInfo.username);
				sendMessage(m);
				addMessage(m);
				
				tb.setText("");
			}
		}
	}
	
	private void sendMessage(Message m) {
		Packet p = new Packet(Header.chat);
		p.message = m;
		
		Client.getClient().server.send(p);
		
	}
	
	public void addMessage(Message msg) {
		messages.add(msg);
		renderChat();
		
	}
	

	@Override
	public void tick() {
		super.animTick();

	}

	@Override
	public void render(Graphics2D graph) {
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setColor(BACKGROUND_COLOR);
		g.fillRoundRect(BUFFER/2, BUFFER/2, rect[2]-BUFFER, rect[3]-BUFFER, BUFFER, BUFFER);
		
		g.setStroke(new BasicStroke(2));
		g.setColor(BORDER_COLOR);
		g.drawRoundRect(BUFFER/2, BUFFER/2, rect[2]-BUFFER, rect[3]-BUFFER, BUFFER, BUFFER);
		
		g.drawImage(chatImg, 0, 0, null);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}
	
	private void renderChat() {
		chatImg = new BufferedImage(rect[2]-BUFFER, rect[3]-BUFFER*9, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) chatImg.getGraphics();
		
		g.setColor(Color.GREEN);
		g.fillRect(BUFFER, BUFFER, chatImg.getWidth(), chatImg.getHeight());
		
	}

}
