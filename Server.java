package aroundearth;

public class Server implements Runnable{
	
	
	public void waitForClient() {
		
	}
	
	public int getVoting() {
		
		return 0;
	}
	
	public void setSkrull() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i = 0 ; i < 5; ++i) {
			waitForClient();
		}
		
		setSkrull();
		
		
		while(true) {

			// Client���� ä�� �ð� �ο�
			
			getVoting();
			
			// Game ���� Ȯ��
			
		}
	}
}

class ChatThread implements Runnable{

	@Override
	// ������� ���⼭ ó��
	// ������ ��� �����ֱ⸸ �ϸ� �ȴ�.
	public void run() {
		// TODO Auto-generated method stub
		
	}
}