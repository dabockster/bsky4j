package bsky4j.api.bsky;

import bsky4j.api.entity.bsky.graph.*;
import bsky4j.api.entity.share.Response;

/**
 * Bluesky/Graph
 * <a href="https://atproto.com/lexicons/app-bsky-graph">Reference</a>
 */
public interface GraphResource {

    Response<GraphFollowResponse> follow(GraphFollowRequest request);

    Response<Void> deleteFollow(GraphDeleteFollowRequest request);

    Response<GraphGetFollowersResponse> getFollowers(GraphGetFollowersRequest request);

    Response<GraphGetFollowsResponse> getFollows(GraphGetFollowsRequest request);

    Response<GraphGetMutesResponse> getMutes(GraphGetMutesRequest request);

    Response<Void> muteActor(GraphMuteActorRequest request);

    Response<Void> unmuteActor(GraphUnmuteActorRequest request);

    Response<GraphBlockResponse> block(GraphBlockRequest request);

    Response<Void> deleteBlock(GraphDeleteBlockRequest request);

    Response<GraphGetBlocksResponse> getBlocks(GraphGetBlocksRequest request);

    Response<Void> getLists();

    Response<Void> getRelationships();

    Response<Void> getActorStarterPacks();

    Response<Void> getList();

    Response<Void> getListBlocks();

    Response<Void> getListMutes();

    Response<Void> getLists();

    Response<Void> getStarterPack();

    Response<Void> getStarterPacks();

    Response<Void> getSuggestedFollowsByActor();

    Response<Void> listblocklist();

    Response<Void> listblockget();

    Response<Void> listblockcreate();

    Response<Void> listblockdelete();

    Response<Void> listitemlist();

    Response<Void> listitemget();

    Response<Void> listitemcreate();

    Response<Void> listitemdelete();

    Response<Void> muteActorList();

    Response<Void> muteThread();

    Response<Void> searchStarterPacks();

    Response<Void> unmuteActorList();

    Response<Void> unmuteThread();
}
