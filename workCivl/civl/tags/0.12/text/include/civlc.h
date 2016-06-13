/* This header file defines standard types and provides
 * functions and function prototypes used in the CIVL-C language.
 * It includes civlc-common.h, and then completes many of the
 * declarations in civlc-common.h.
 */
 
#ifdef __CIVLC__
#else
#define __CIVLC__
#include<civlc-common.h>

/* ******************************* Types ******************************* */

/* A message formed by $message_pack.  Completes the declaration
 * of this structure type in civlc-common.h */
struct __message__ {
  int source;
  int dest;
  int tag;
  $bundle data;
  int size;
};

/* A datatype representing a queue of messages.  All message
 * data is encapsulated inside this value; no external allocation
 * is used.  Completes the declaration of this structure type in 
 * civlc-common.h */ 
struct __queue__ {
  int length;
  $message messages[];
};


/* A global communicator datatype which must be operated by local communicators.
 * This communicator type has the same meaning as the communicator type
 * in MPI.  Completes the declaration of this type in civlc-common.h */
struct __gcomm__ {
  int nprocs; // number of processes
  _Bool isInit[]; // if the local comm has been initiated
  $queue buf[][]; // message buffers
};

/* A datatype representing a local communicator which is used for 
 * operating global communicators. The local communicator type has 
 * a handle of a global communicator. This type represents for 
 * a set of processes which have ranks in common.
 * Completes the declaration of this type in civlc-common.h.
 */
struct __comm__ {
  int place;
  $gcomm gcomm;
};

/* A datatype representing a global barrier which must be operated by local
 * barriers.  Completes the declaration of this type in civlc-common.h.
 */
struct __gbarrier__ {
  int nprocs;
  $proc proc_map[]; // initialized as all $proc_null.
  _Bool in_barrier[]; // initialized as all false.
  int num_in_barrier; // initialized as 0.
};

/* A datatype representing a global barrier which used for 
 * operating global barriers. The local barrier type has 
 * a handle of a global barrier.
 * Completes the declaration of this type in civlc-common.h.
 */
struct __barrier__ {
  int place;
  $gbarrier gbarrier; // initialized as 0.
};

/* Completes the declaration of this type in civlc-common.h */
struct __int_iter__ {
  int size;
  int content[];
  int index; //initialized as 0
};

/* ***************************** Functions ***************************** */


/* creates a new message, copying data from the specified buffer */ 
$message $message_pack(int source, int dest, int tag,
    void *data, int size) {
  $message result;
  
  result.source = source;
  result.dest = dest;
  result.tag = tag;
  result.data = $bundle_pack(data, size);
  result.size = size;
  return result;
}
  
/* returns the message source */ 
int $message_source($message message) {
  return message.source;
}

/* returns the message tag */
int $message_tag($message message) {
  return message.tag;
}

/* returns the message destination */ 
int $message_dest($message message) {
  return message.dest;
}

/* returns the message size */ 
int $message_size($message message) {
  return message.size;
}

/* transfers message data to buf, throwing exception if message
 * size exceeds specified size */ 
void $message_unpack($message message, void *buf, int size) {
  $bundle_unpack(message.data, buf);
  $assert(message.size <= size, 
    "Message of size %d exceeds the specified size %d.", message.size, size);
}

/* Returns the place of the local communicator.  This is the same as the
 * place argument used to create the local communicator. */
int $comm_place($comm comm){
 return comm->place;
}

void $barrier_call($barrier barrier) {
  $barrier_enter(barrier);
  $barrier_exit(barrier);
}

#endif
