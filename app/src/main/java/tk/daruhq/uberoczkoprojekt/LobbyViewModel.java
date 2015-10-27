package tk.daruhq.uberoczkoprojekt;

/**
 * Created by Patryk on 2015-10-27.
 */
public class LobbyViewModel {

    private int scores;
    private int membersCount;
    private String name;
    private String ownerNickname;
    private int maxMembersCount;

    public LobbyViewModel(int membersCount, String name, String ownerNickname, int maxMembersCount)
    {
        super();
        this.membersCount = membersCount;
        this.name = name;
        this.ownerNickname = ownerNickname;
        this.maxMembersCount = maxMembersCount;
    }

    public int getScores() {
        return scores;
    }

    public void setScores(int scores) {
        this.scores = scores;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public int getMaxMembersCount() {
        return maxMembersCount;
    }

    public void setMaxMembersCount(int maxMembersCount) {
        this.maxMembersCount = maxMembersCount;
    }
}
