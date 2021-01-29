package samb.client.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Window;

public class PageManager {
	
	private Page curPage;
	
	private BufferedImage prevPageImg;
	private int xoff = 0, transitionSpeed=45;

	public PageManager() {
		this.curPage = new LoginPage();

	}
	
	public Page get() {
		return curPage;
		
	}
	
	public void tick() {
		curPage.tick();
		pageTransition();
		
	}
	
	private void pageTransition() {
		if(xoff > 0) {
			xoff -= transitionSpeed;
			
			if(xoff < 0) {
				xoff = 0;
				prevPageImg = null;
			}
		}
	}
	
	
	public void render(Graphics2D g) {
		if(prevPageImg != null) {
			g.drawImage(prevPageImg, xoff-Window.dim.width, 0, null);
		}
		
		g.drawImage(curPage.getRender(), xoff, 0, null);
		
	}
	
	public void changePage(Page newPage) {
		this.prevPageImg = curPage.getRender();
		this.xoff = Window.dim.width;
		
		this.curPage = newPage;
		
	}
	
	public boolean isId(String id) {
		return get().id.equals(id);
		
	}
	
}
