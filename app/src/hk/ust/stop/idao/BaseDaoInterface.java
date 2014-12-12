package hk.ust.stop.idao;

import java.util.ArrayList;

import hk.ust.stop.model.GoodsInformation;
import hk.ust.stop.model.UserInformation;


/**
 * This interface define some operation of database
 * @author XJR
 *
 */
public interface BaseDaoInterface {
	/**
	 * Insert a record with user's id
	 * @param user if null, userId attribute would be 0
	 * @param info goods information
	 * @param flag to decide whether this product is upload by user or other users
	 */
	public boolean insert(UserInformation user, GoodsInformation info, int flag);
	
	
	/**
	 * Query all record with user's id and the flag
	 * @param user if null, userId attribute would be 0, and that means the 
	 *        user havn't logged in.
	 * @param flag to decide to query user's uploaded product or purchased product
	 * @return
	 */
	public ArrayList<GoodsInformation> queryAllRecord(UserInformation user, int flag);

	
	/**
	 * Delete a record according to the userId and GoodsId
	 * @param user
	 * @param goods
	 * @return the result of delete
	 */
	public boolean deleteByUserAndGoodsId(UserInformation user, GoodsInformation goods);
}
