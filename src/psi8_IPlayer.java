import java.util.ArrayList;

public class psi8_IPlayer extends psi8_Player {
    private ArrayList<psi8_State> states = new ArrayList<psi8_State>();
    private psi8_State currentState;

    public psi8_IPlayer(int id,int states) {
        super(id);
        for (int i = 0; i < states; i++) {
            this.states.add(new psi8_State(i, 4));
        }
    }

    public psi8_State getCurrentState() {
        return currentState;
    }

    public psi8_State getNextState() {
        if (currentState.getPosition() == 0) {
            return states.get(states.size() - 1);
        } else {
            return states.get(currentState.getPosition() - 1);
        }
    }

    public psi8_State getPrevState() {
        if (currentState.getPosition() == (states.size() - 1)) {
            return states.get(0);
        } else {
            return states.get(currentState.getPosition() + 1);
        }
    }

    public void setCurrentState(int position) {
        currentState = states.get(position);
    }

    public ArrayList<psi8_State> getStates(){
        return states;
    }
}
