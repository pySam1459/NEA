package samb.client.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Window;

public class PageManager {
	/* This class is used to manage the pages, 
	 *   acts as an interface for the page being displayed
	 *   abstracts the specifics of each page
	 * */
	
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
		// Scrolls xoff, leftwards
		if(xoff > 0) {
			xoff -= transitionSpeed;
			
			if(xoff < 0) {
				xoff = 0;
				prevPageImg = null;
			}
		}
	}
	
	
	public void render(Graphics2D g) {
		// This method renders the page / pages
		if(prevPageImg != null) {
			g.drawImage(prevPageImg, xoff-Window.dim.width, 0, null);
		}
		
		g.drawImage(curPage.getRender(), xoff, 0, null);
	}
	
	public void changePage(Page newPage) {
		// This method is called when a new page is to be displayed
		
		this.prevPageImg = curPage.getRender();
		this.xoff = Window.dim.width;
		
		this.curPage = newPage;
		
	}
	
	public boolean isId(String id) { // Checks whether the current page's id is == to param 'id'
		return get().id.equals(id);
		
	}
	
}
