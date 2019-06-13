package com.dreamsecurity.ca.business.group.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamsecurity.ca.business.common.CommonConstants;
import com.dreamsecurity.ca.business.common.domain.Criteria;
import com.dreamsecurity.ca.business.group.dao.GroupDao;
import com.dreamsecurity.ca.business.group.vo.GroupSolutionVo;
import com.dreamsecurity.ca.business.group.vo.GroupVo;
import com.dreamsecurity.ca.business.group.vo.UserGroupVo;
import com.dreamsecurity.ca.business.login.common.LoginConstants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
public class GroupService {

	@Resource
	private GroupDao dao;

	@Transactional(rollbackFor={Exception.class})
	public void registerGroup( HttpServletRequest request ) throws JsonParseException, JsonMappingException, IOException {
		HttpSession session = request.getSession();
		JSONObject body = (JSONObject) request.getAttribute( "body" );
		GroupVo groupVo = GroupVo.deserialize( body );
		Date currnetDate = new Date();

		groupVo.setCreateDate( currnetDate );
		groupVo.setState( 3 );
		groupVo.setCreator( session.getAttribute( LoginConstants.SESSION_ID).toString() );

		dao.registerGroup( groupVo );

		UserGroupVo userGroupVo = new UserGroupVo();

		userGroupVo.setUserId( groupVo.getCreator() );
		userGroupVo.setGroupId( groupVo.getId() );
		userGroupVo.setJoinDate( currnetDate );
		userGroupVo.setUserAuthority( 0 );
		userGroupVo.setState( 0 );

		dao.addUserToGroup( userGroupVo );

		GroupSolutionVo groupSolutionVo = new GroupSolutionVo();

		groupSolutionVo.setGroupId( groupVo.getId() );
		groupSolutionVo.setSolutionName( groupVo.getGroupSolutionName() );
		groupSolutionVo.setState( 4 );
		groupSolutionVo.setCreateDate( currnetDate );
		groupSolutionVo.setCreator( LoginConstants.SESSION_ID );

		dao.insertGroupSolution( groupSolutionVo );

	}

	public List<Map<String, Object>> showGroupList( HttpServletRequest request, Criteria cri ) {
		List<Map<String,Object>> voMapList = dao.selectGroupList( cri );

		for ( Map<String, Object> voMap : voMapList ) {
			voMap.put( "createdate", CommonConstants.dateFormat.format( (Date)voMap.get( "createdate" ) ) );
		}

		return voMapList;
	}

	public int showGroupListCnt() {
		return dao.selectGroupListCnt();
	}

	public List<Map<String, Object>> showUserGroupApplyList( HttpServletRequest request, Criteria cri, int groupId ) {
		List<Map<String,Object>> voMapList = dao.selectUserGroupApplyList( cri, groupId );

		for ( Map<String, Object> voMap : voMapList ) {
			voMap.put( "joindate", CommonConstants.dateFormat.format( (Date)voMap.get( "joindate" ) ) );
		}

		return voMapList;
	}

	public int showUserGroupApplyListCnt( int groupId ) {
		return dao.selectUserGroupApplyListCnt( groupId );
	}

	public void approveUserToJoinGroup( HttpServletRequest request, int groupId, String userId ) throws IllegalAccessException {
		HttpSession session = request.getSession();
		UserGroupVo doUserVo = new UserGroupVo();

		doUserVo.setUserId( session.getAttribute( LoginConstants.SESSION_ID ).toString() );
		doUserVo.setGroupId( groupId );

		if ( dao.selectGroupMaster( doUserVo ) == null ) throw new IllegalAccessException( "permission denided..." );

		UserGroupVo vo = new UserGroupVo();

		vo.setUserId( userId );
		vo.setGroupId( doUserVo.getGroupId() );
		vo.setState( 1 );

		dao.updateUserState( vo );
	}

	public List<UserGroupVo>showUserList( int groupId ) throws JsonParseException, JsonMappingException, IOException {
		UserGroupVo vo = new UserGroupVo();
		vo.setGroupId( groupId );

		return dao.selectUserGroupList( vo );
	}

	public void addUserToGroup( HttpServletRequest request, int groupId ) throws JsonParseException, JsonMappingException, IOException {
		UserGroupVo userGroupVo = new UserGroupVo();

		userGroupVo.setGroupId( groupId );
		userGroupVo.setUserId( request.getSession().getAttribute( LoginConstants.SESSION_ID ).toString() );
		userGroupVo.setJoinDate( new Date() );
		userGroupVo.setUserAuthority( 0 );
		userGroupVo.setState( 0 );

		dao.insertUserToGroup( userGroupVo );
	}

	public void removeUserToGroup( HttpServletRequest request, int groupId, String userId ) throws JsonParseException, JsonMappingException, IOException {
		UserGroupVo userGroupVo = new UserGroupVo();

		userGroupVo.setGroupId( groupId );
		userGroupVo.setUserId( userId );

		dao.deleteUserToGroup( userGroupVo );
	}

	public void applyGroupSolution( HttpServletRequest request, int groupId ) throws JsonParseException, JsonMappingException, IOException {
		JSONObject body = (JSONObject)request.getAttribute( "body" );
		GroupSolutionVo vo = new GroupSolutionVo();

		if ( !body.has( "solutionName" ) )
			throw new IllegalArgumentException( "solutionName is null." );

		vo.setSolutionName( body.getString( "solutionName" ) );
		vo.setGroupId( groupId );
		vo.setState( 3 );
		vo.setCreateDate( new Date() );
		vo.setCreator( request.getSession().getAttribute( LoginConstants.SESSION_ID ).toString() );

		dao.insertGroupSolution( vo );
	}
}
